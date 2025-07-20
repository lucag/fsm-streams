package com.gvolpe.fsmstreams.analytics

import com.gvolpe.fsmstreams.analytics.Ticker.Count
import com.gvolpe.fsmstreams.game.*
import com.gvolpe.fsmstreams.game.types.*

import cats.*
import cats.effect.*
import cats.syntax.all.*
import fs2.*
import cats.Applicative

case class Engine[F[_] : {Concurrent, Parallel, Time}](
    publish: Summary => F[Unit],
    ticker:  Ticker[F]
) {
  private val fsm = Engine.fsm[F](ticker)

  def run: Pipe[F, Event, Unit] =
    _.noneTerminate
      .zip(ticker.ticks)
      .evalMapAccumulate(Map.empty[PlayerId, Agg] -> 0)(fsm.run)
      .collect { case (_, (out, Tick.On)) => out }
      .evalMap: m =>
        Time[F].timestamp.flatMap: ts =>
          m.toList.parTraverse_ : (pid, agg) =>
            publish(agg.summary(pid, ts))
}

object Engine {
  type Result = (Map[PlayerId, Agg], Tick)
  type State  = (Map[PlayerId, Agg], Count)

  // import com.gvolpe.fsmstreams.game.Types.Points
  import Agg.*

  def fsm[F[_] : Applicative](ticker: Ticker[F]): FSM[F, State, (Option[Event], Tick), Result] =
    FSM {
      case ((m, count), (Some(event), tick)) =>
        val (playerId, modifier) =
          event match
            case Event.LevelUp(pid, l, _)            =>
              pid -> points.modify(_ + 100)
                .andThen(level.replace(l))
            case Event.PuzzleSolved(pid, _, _, _)    =>
              pid -> points.modify(_ + 50)
            case Event.GemCollected(pid, gemType, _) =>
              pid -> points.modify(_ + 10)
                .andThen {
                  gems.modify:
                    _.updatedWith(gemType):
                      _.map(_ + 1) orElse Some(1)
                }

        val agg = m.getOrElse(playerId, Agg.empty)
        val out = m.updated(playerId, modifier(agg))
        val nst = if tick === Tick.On then Map.empty[PlayerId, Agg] else out

        ticker.merge(tick, count).map: (newTick, newCount) =>
          (nst -> newCount) -> (out -> newTick)

      case ((m, _), (None, _)) =>
        Applicative[F].pure((Map.empty -> 0) -> (m -> Tick.On))
    }

}
