// package com.gvolpe.fsmstreams
//
// import scala.concurrent.ExecutionContext
//
// import cats.effect.*
// import munit.*
//
// trait IOSuite extends ScalaCheckSuite {
// //  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
// //  implicit val timer: Timer[IO]     = IO.timer(ExecutionContext.global)
//
//   override def munitValueTransforms: List[ValueTransform] =
//     super.munitValueTransforms :+ new ValueTransform(
//       "IO",
//       {
//         case ioa: IO[?] => IO.suspend(ioa).unsafeToFuture()
//       }
//     )
//
//   def ioTest[A](ioa: IO[A]): A = ioa.unsafeRunSync()
// }
