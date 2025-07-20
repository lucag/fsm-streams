//package com.gvolpe.fsmstreams
//package analytics
//
//import cats.*
//import cats.effect.*
//import cats.syntax.all.*
//import munit.{ CatsEffectSuite, ScalaCheckEffectSuite }
//
//import scala.concurrent.duration.*
//
//class TickerSuiteAlt extends CatsEffectSuite with ScalaCheckEffectSuite {
//
//  test("Ticker emits timer ticks every specified time — alternate") {
//    import cats.effect.testkit.TestControl
//
//    def prog(control: TestControl[Unit]) =
//      for
//        ticker      <- Ticker.create[IO](10, 2.seconds)
//        gate        <- Deferred[IO, Either[Throwable, Unit]]
//        counter     <- Ref.of[IO, Int](0)
//        syncer      <- Deferred[IO, Unit]
//        syncAttempt  = counter
//                         .updateAndGet(_ + 1)
//                         .flatMap: n =>
//                           syncer
//                             .complete(())
//                             .attempt
//                             .void
//                             .whenA(n > 20)
//        counterTick <- ticker.get
//        fb          <- ticker
//                         .ticks
//                         .interruptWhen(gate)
//                         .zipWithIndex
//                         .evalTap { _ => syncAttempt }
//                         .evalTap { (t, n) => IO.println(s"Tick: $t at $n") }
//                         .compile
//                         .toList
//                         .start
//        _           <- control.advance(2.seconds + 50.millis)
//        _           <- syncer.get // let the ticks run
//        _           <- gate.complete(().asRight)
//        timerTicks  <- fb.joinWithNever
//      yield {
//        assertEquals(counterTick, Tick.Off)
//        assertEquals(
//          timerTicks.count: (t, _) =>
//            t === Tick.On,
//          1
//        )
//      }
//      end for
//    end prog
//
//    TestControl.execute(IO.never) >>= prog
//  }
//
//}
