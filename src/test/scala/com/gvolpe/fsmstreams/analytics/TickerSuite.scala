package com.gvolpe.fsmstreams
package analytics

import cats.*
import cats.effect.*
import cats.syntax.all.*
import munit.{ CatsEffectSuite, ScalaCheckEffectSuite }

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*

class TickerSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private var clockValue: FiniteDuration =
    FiniteDuration(System.nanoTime(), TimeUnit.NANOSECONDS)

  private def tick(d: FiniteDuration): IO[Unit] = IO:
    clockValue = clockValue + d

  given Clock[IO] = new Clock[IO]:
    def monotonic: IO[FiniteDuration] = IO.pure(clockValue)
    def realTime: IO[FiniteDuration]  = IO.pure(clockValue)
    def applicative: Applicative[IO]  = new Applicative[IO]:
      def pure[A](x: A): IO[A]                       = IO.pure(x)
      def ap[A, B](ff: IO[A => B])(fa: IO[A]): IO[B] =
        ff.flatMap(f => fa.map(f))

  test("Ticker emits timer ticks every specified time") {
    for
      ticker      <- Ticker.create[IO](10, 2.seconds)
      gate        <- Deferred[IO, Either[Throwable, Unit]]
      counter     <- Ref.of[IO, Int](0)
      syncer      <- Deferred[IO, Unit]
      syncAttempt  = counter
                       .updateAndGet(_ + 1)
                       .flatMap: n =>
                         syncer
                           .complete(())
                           .attempt
                           .void
                           .whenA(n > 20)
      counterTick <- ticker.get
      fb          <- ticker
                       .ticks
                       .interruptWhen(gate)
                       .zipWithIndex
                       .evalTap { _ => syncAttempt }
//                       .evalTap { (t, n) => IO.println(s"Tick: $t at $n") }
                       .compile
                       .toList
                       .start
      _           <- tick(2.seconds + 50.millis)
      _           <- syncer.get // let the ticks run
      _           <- gate.complete(().asRight)
      timerTicks  <- fb.joinWithNever
    yield {
      assertEquals(counterTick, Tick.Off)
      assertEquals(
        timerTicks.count: (t, _) =>
          t === Tick.On,
        1
      )
    }
  }

  test("Ticker emits counter ticks every specified number of events") {
    for
      ticker       <- Ticker.create[IO](10, 2.seconds)
      gate         <- Deferred[IO, Either[Throwable, Unit]]
      counter      <- Ref.of[IO, Int](0)
      syncer       <- Deferred[IO, Unit]
      syncAttempt   =
        counter.updateAndGet(_ + 1).flatMap(n => syncer.complete(()).attempt.void.whenA(n > 20))
      counterTick1 <- ticker.get
      fb           <- ticker.ticks.interruptWhen(gate).evalTap(_ => syncAttempt).compile.toList.start
      _            <- tick(2.seconds.plus(10.millis))
      _            <- syncer.get // let the ticks run
      _            <- ticker.merge(Tick.Off, 10)
      _            <- gate.complete(Right(()))
      allTicks     <- fb.joinWithNever
      counterTick2 <- ticker.get
    yield {
      assertEquals(counterTick1, Tick.Off)
      assertEquals(counterTick2, Tick.On)
      assertEquals(allTicks.count(_ === Tick.On), 1)
    }
  }

}
