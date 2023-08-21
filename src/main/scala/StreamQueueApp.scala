package me.binwang.demo.stream

import cats.effect.{ExitCode, IO}
import cats.implicits._
import fs2.concurrent.{Queue, SignallingRef}

import java.util.concurrent.atomic.AtomicInteger

object StreamQueueApp extends BaseStreamApp {

  private val counter = new AtomicInteger(0)

  override def run(args: List[String]): IO[ExitCode] = {
    val task = for{
      queue <- Queue.bounded[IO, Int](batchSize * 2)
      stopSignal <- SignallingRef[IO, Boolean](false)
      stream <- Seq(
        produceStream(0).through(queue.enqueue).compile.drain,
        queue.dequeue.interruptWhen(stopSignal).parEvalMap(batchSize){ x =>
          consume(x).flatMap(_ => if (counter.incrementAndGet() >= totalSize) stopSignal.set(true) else IO.pure())
        }.compile.drain,
      ).parSequence
    } yield stream
    printElapsedTime(task).map(_ => ExitCode.Success)
  }


}
