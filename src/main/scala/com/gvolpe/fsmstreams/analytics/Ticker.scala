package com.gvolpe.fsmstreams.analytics

import scala.concurrent.duration.*

import Ticker.Count

import cats.*
import cats.derived.*
import cats.effect.*
import cats.effect.Ref
import cats.syntax.all.*
import fs2.Stream

/** Either emit a tick when we reach a `TimeWindow` or when we reach the `MaxNumberOfEvents`.
  *
  * In any case, both the timer and the counter should be restarted.
  */
trait Ticker[F[_]] {

  /** @return the current value of the counter Tick. */
  def get: F[Tick]

  /** @param count
    *   the current value of the counter
    * @param timerTick
    *   the current value of the timer Tick, emitted by [[ticks]]
    *
    * @return
    *   the combination of the Tick (according to its Semigroup instance) and the updated value
    *   of the count, which could be either increased by one or resetted to zero.
    */
  def merge(timerTick: Tick, count: Count): F[(Tick, Count)]

  /** @return
    *   a continuos stream of Ticks. Its value is ON either when the `TimeWindow` has passed or
    *   when the `MaxNumberOfEvents` has been reached. Its value is OFF, otherwise.
    */
  def ticks: Stream[F, Tick]
}

object Ticker {
  type Count = Int

  def create[F[_] : {Clock, Concurrent}](
      maxNrOfEvents: Int,
      timeWindow: FiniteDuration
  ): F[Ticker[F]] =
    Ref.of[F, Tick](Tick.Off).map {
      ref =>
        new Ticker[F] {
          def get: F[Tick] = ref.get

          def merge(timerTick: Tick, count: Count): F[(Tick, Count)] =
            ref
              .modify {
                case Tick.Off if count === maxNrOfEvents => Tick.On  -> 0
                case _ if timerTick === Tick.On          => Tick.Off -> 0
                case _                                   => Tick.Off -> (count + 1)
              }
              .flatMap {
                newCount =>
                  get.map {
                    counterTick =>
                      val newTick = counterTick |+| timerTick
                      newTick -> newCount
                  }
              }

          def ticks: Stream[F, Tick] = {
            val interval = FiniteDuration((timeWindow.toSeconds * 0.05).toLong, SECONDS)

            def go(lastSpikeNanos: FiniteDuration): Stream[F, Tick] =
              Stream.eval { (Clock[F].monotonic, get).tupled }.flatMap: (now, tick) =>
                if
                  (now - lastSpikeNanos) > timeWindow
                  || (tick === Tick.On && (now - lastSpikeNanos) > interval)
                then
                  Stream.emit(Tick.On) ++ go(now)
                else
                  Stream.emit(Tick.Off) ++ go(lastSpikeNanos)

            go(FiniteDuration(0, SECONDS)).tail
          }

        }
    }
}

enum Tick derives Eq, Show:
  case On
  case Off

object Tick {
  given Semigroup[Tick] =
    new Semigroup[Tick] {
      def combine(x: Tick, y: Tick): Tick = (x, y) match
        case (On, _)    => On
        case (_, On)    => On
        case (Off, Off) => Off
    }
}
