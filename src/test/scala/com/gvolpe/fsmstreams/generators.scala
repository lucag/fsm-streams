package com.gvolpe.fsmstreams

import java.time.Instant
import java.util.UUID

import game.*
import game.types.*

import org.scalacheck.Gen

object generators {

  val nonEmptyString: Gen[String] =
    Gen
      .chooseNum(21, 40)
      .flatMap { n => Gen.buildableOfN[String, Char](n, Gen.alphaChar) }

  val timestamp: Gen[Timestamp] =
    Gen
      .choose[Long](
        Instant.parse("1980-01-01T00:00:00.000Z").getEpochSecond,
        Instant.parse("3000-01-01T00:00:00.000Z").getEpochSecond
      )
      .map(millis => Timestamp(Instant.ofEpochMilli(millis)))

  val uuidPool: List[UUID] = List.fill(2)(UUID.randomUUID())

  val genPlayerIdFromPool: Gen[PlayerId] =
    Gen.oneOf(uuidPool.map(PlayerId.apply))

  val genLevelUp: Gen[Event.LevelUp] =
    for
      id <- genPlayerIdFromPool
      lv <- Gen.posNum[Int].map(Level(_))
      ts <- timestamp
    yield Event.LevelUp(id, lv, ts)

  val genPuzzleSolved: Gen[Event.PuzzleSolved] =
    for
      id <- genPlayerIdFromPool
      pn <- nonEmptyString.map(PuzzleName(_))
      fd <- Gen.finiteDuration
      ts <- timestamp
    yield Event.PuzzleSolved(id, pn, fd, ts)

  val genGemType: Gen[GemType] =
    Gen.oneOf[GemType](
      List(GemType.Diamond, GemType.Emerald, GemType.Ruby, GemType.Sapphire)
    )

  val genGemCollected: Gen[Event.GemCollected] =
    for
      id <- genPlayerIdFromPool
      gt <- genGemType
      ts <- timestamp
    yield Event.GemCollected(id, gt, ts)

  val genEvent: Gen[Event] =
    Gen.oneOf(genLevelUp, genPuzzleSolved, genGemCollected)

}
