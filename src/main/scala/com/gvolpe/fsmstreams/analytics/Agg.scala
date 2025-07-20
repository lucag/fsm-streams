package com.gvolpe.fsmstreams.analytics

import com.gvolpe.fsmstreams.game.*
import com.gvolpe.fsmstreams.game.types.*

import monocle.macros.*

case class Agg(level: Level, points: Points, gems: Map[GemType, Int]) {
  def summary(pid: PlayerId, ts: Timestamp): Summary =
    Summary(pid, level, points, gems, ts)
}

object Agg {
  def empty = Agg(Level(0), Points(0), Map.empty)

  val gems   = GenLens[Agg](_.gems)
  val level  = GenLens[Agg](_.level)
  val points = GenLens[Agg](_.points)
}

case class Summary(
    playerId:  PlayerId,
    level:     Level,
    points:    Points,
    gems:      Map[GemType, Int],
    createdAt: Timestamp
)
