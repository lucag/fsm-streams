package com.gvolpe.fsmstreams
package analytics

import cats.*
import cats.effect.*
import cats.effect.Ref
import cats.syntax.all.*
import fs2.Stream
import game.Event
import game.types.{Level, Points}
import generators.*
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.{Gen, Test}

import scala.concurrent.duration.*
import org.scalacheck.effect.PropF

class EngineSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  override def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters.withMinSuccessfulTests(10)

  test("Aggregate game events by player id") {
    PropF.forAllF(Gen.nonEmptyListOf(genEvent)): events =>
      for
        ref    <- Ref.of[IO, List[Summary]](List.empty)
        ticker <- Ticker.create[IO](500, 2.seconds)
        engine  = Engine[IO](s => ref.update(_ :+ s), ticker)
        _      <- engine.run(Stream.emits(events)).compile.drain
        result <- ref.get
      yield {
        val levelUpEvents      = events.collect { case e: Event.LevelUp => e }.size
        val puzzleSolvedEvents = events.collect { case e: Event.PuzzleSolved => e }.size
        val gemCollectedEvents = events.collect { case e: Event.GemCollected => e }.size

        val score = Points(
          levelUpEvents * 100
            + puzzleSolvedEvents * 50
            + gemCollectedEvents * 10
        )

        val numberOfPlayers = events.groupBy(e => Event.playerId.headOption(e)).size

        val level = Event.lastLevelSum.fold(events)

        assertEquals(result.map(_.points).fold(Points(0))(_ combine _), score)
        assertEquals(result.size, numberOfPlayers)
        assertEquals(result.map(_.level).fold(Level(0))(_ combine _), level)
        assertEquals(result.map(_.gems.values.toList.sum).sum, gemCollectedEvents)
      }
  }

}
