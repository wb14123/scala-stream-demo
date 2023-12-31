package me.binwang.demo.stream

import cats.effect.{Blocker, ContextShift, IO, Timer}
import cats.implicits._
import fs2.concurrent.Queue

import java.util.concurrent.atomic.AtomicInteger

class ConcurrentProducerQueueApp(config: TestConfig)(
  implicit timer: Timer[IO], contextShift: ContextShift[IO], blocker: Blocker) extends TestRunner(config) {

  override val name = "stream with fs2 queue and concurrent producer"

  private val counter = new AtomicInteger(0)

  override def work(): IO[Unit] = {
    for {
      queue <- Queue.bounded[IO, Int](config.batchSize * 2)
      _ <- Seq(
        produceStream(0, config.totalSize / 2).through(queue.enqueue).compile.drain,
        produceStream(config.totalSize / 2, config.totalSize).through(queue.enqueue).compile.drain,
        queue.dequeue.parEvalMap(config.batchSize) { x =>
          consume(x).map { _ =>
            if (counter.incrementAndGet() >= config.totalSize) {
              None
            } else {
              Some()
            }
          }
        }.unNoneTerminate.compile.drain,
      ).parSequence
    } yield ()
  }


}

