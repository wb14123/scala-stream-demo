package me.binwang.demo.stream

import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import fs2.concurrent.Queue

class StreamQueueApp(config: TestConfig)(implicit timer: Timer[IO], contextShift: ContextShift[IO]) extends TestRunner(config) {

  override val name = "stream with fs2 queue"

  override def work(): IO[Unit] = {
    for {
      queue <- Queue.bounded[IO, Option[Int]](config.batchSize * 2)
      _ <- Seq(
        (produceStream(0).map(Some(_)) ++ fs2.Stream.emit(None)).through(queue.enqueue).compile.drain,
        queue.dequeue.unNoneTerminate.parEvalMap(config.batchSize)(consume).compile.drain,
      ).parSequence
    } yield ()
  }


}
