package com.gvolpe.fsmstreams.game

import java.time.Instant
import java.util.UUID
import cats.kernel.Monoid

object types {

  opaque type Level = Int
  object Level {
    def apply(level: Int): Level = level

    given Monoid[Level] with
      def empty: Level                       = Level(0)
      def combine(x: Level, y: Level): Level = Level(x + y)
  }

  opaque type Points = Int
  object Points {
    def apply(points: Int): Points = points

    given Monoid[Points] with
      def empty: Points                       = Points(0)
      def combine(x: Points, y: Points): Points = Points(x + y)

    extension (points: Points)
      def + (x: Int): Points = Points(points + x)
      def - (x: Int): Points = Points(points - x)
      def * (x: Int): Points = Points(points * x)
  }

  opaque type PlayerId = UUID
  object PlayerId {
    def apply(id: UUID): PlayerId = id
  }

  opaque type PlayerScore = Int
  object PlayerScore {
    def apply(score: Int): PlayerScore = score
  }

  opaque type PuzzleName = String
  object PuzzleName {
    def apply(name: String): PuzzleName = name
  }

  opaque type Timestamp = Instant
  object Timestamp {
    def apply(instant: Instant): Timestamp = instant
  }
}
