package com.gvolpe.fsmstreams.game

import com.gvolpe.fsmstreams.game.types.*

case class Player(
    id:           PlayerId,
    highestScore: PlayerScore,
    gems:         Map[GemType, Int],
    memberSince:  Timestamp
)
