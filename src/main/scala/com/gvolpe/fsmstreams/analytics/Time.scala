package com.gvolpe.fsmstreams.analytics

import java.time.Instant

import cats.effect.Sync
import com.gvolpe.fsmstreams.game.types.Timestamp

trait Time[F[_]] {
  def timestamp: F[Timestamp]
}

object Time {
  def apply[F[_] : Time]: Time[F] = new Time[F] {
    def timestamp: F[Timestamp] = implicitly[Time[F]].timestamp
  }

  given [F[_] : Sync]: Time[F] =
    new Time[F] {
      def timestamp: F[Timestamp] =
        Sync[F].delay(Timestamp(Instant.now()))
    }
}
