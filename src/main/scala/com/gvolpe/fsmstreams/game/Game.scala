package com.gvolpe.fsmstreams.game

import com.gvolpe.fsmstreams.game.types.*

case class Game(
    playerId:    PlayerId,
    playerScore: PlayerScore,
    level:       Level,
    gems:        Map[GemType, Int]
)
