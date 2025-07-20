package com.gvolpe.fsmstreams.game

import scala.concurrent.duration.*
import cats.Monoid
import monocle.{ Fold, Prism }
import monocle.macros.*
import com.gvolpe.fsmstreams.game.types.*

enum Event:
  def createdAt: Timestamp

  case LevelUp(
      playerId:  PlayerId,
      newLevel:  Level,
      createdAt: Timestamp
  )

  case PuzzleSolved(
      playerId:   PlayerId,
      puzzleName: PuzzleName,
      duration:   FiniteDuration,
      createdAt:  Timestamp
  )

  case GemCollected(
      playerId:  PlayerId,
      gemType:   GemType,
      createdAt: Timestamp
  )

object Event:
  val gemCollected: Prism[Event, GemCollected] = GenPrism[Event, GemCollected]
  val levelUp: Prism[Event, LevelUp]           = GenPrism[Event, LevelUp]
  val puzzleSolved: Prism[Event, PuzzleSolved] = GenPrism[Event, PuzzleSolved]

  val playerId: Fold[Event, PlayerId] =
    new Fold[Event, PlayerId]:
      def foldMap[M : Monoid](f: PlayerId => M)(s: Event): M =
        s match
          case LevelUp(pid, _, _)         => f(pid)
          case PuzzleSolved(pid, _, _, _) => f(pid)
          case GemCollected(pid, _, _)    => f(pid)

  import cats.*
  import cats.syntax.all.*

  val lastLevelSum: Fold[List[Event], Level] =
    new Fold[List[Event], Level] {
      def foldMap[M : Monoid](f: Level => M)(s: List[Event]): M =
        f {
          s.flatMap(levelUp.getOption(_).toList)
            .groupBy(_.playerId)
            .map: (_, xs) =>
              xs.map(_.newLevel)
                .takeRight(1)
                .headOption
                .getOrElse(Level(0))
            .fold(Level(0))(_ combine _)
        }
    }

end Event
