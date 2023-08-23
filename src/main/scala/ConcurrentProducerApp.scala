package me.binwang.demo.stream

import cats.effect.{Blocker, ContextShift, IO, Timer}
import cats.implicits._
import fs2.concurrent.{Queue, SignallingRef}

import java.util.concurrent.atomic.AtomicInteger

class ConcurrentProducerApp(config: TestConfig)(
  implicit timer: Timer[IO], contextShift: ContextShift[IO], blocker: Blocker) extends TestRunner(config) {

  override val name = "stream with fs2 queue with concurrent producer"

  private val counter = new AtomicInteger(0)

  override def work(): IO[Unit] = {
    for {
      queue <- Queue.bounded[IO, Int](config.batchSize * 2)
      stopSignal <-  SignallingRef[IO, Boolean](false)
      _ <- Seq(
        produceStream(0, config.totalSize / 2).through(queue.enqueue).compile.drain,
        produceStream(config.totalSize / 2).through(queue.enqueue).compile.drain,
        queue.dequeue.interruptWhen(stopSignal).parEvalMap(config.batchSize) {x =>
          consume(x).flatMap { _ =>
            if (counter.incrementAndGet() >= config.totalSize) stopSignal.set(true) else IO.pure()
          }
        }.compile.drain,
      ).parSequence
    } yield ()
  }


}
