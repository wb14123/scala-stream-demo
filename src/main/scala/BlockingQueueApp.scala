package me.binwang.demo.stream

import cats.effect.IO
import cats.implicits._

import java.util.concurrent.LinkedBlockingQueue

class BlockingQueueApp(config: TestConfig) extends TestRunner(config) {

  override val name = "blocking queue"

  private val queue = new LinkedBlockingQueue[Option[Int]](config.batchSize * 2)

  override def work(): IO[Unit] = {
    Seq(
      (produceStream(0).map(Some(_)) ++ fs2.Stream.emit(None)).evalMap(x => IO(queue.put(x))).compile.drain,
      dequeueStream().unNoneTerminate.parEvalMap(config.batchSize)(consume).compile.drain,
    ).parSequence.map(_ => ())
  }

  private def dequeueStream(): fs2.Stream[IO, Option[Int]] = {
    fs2.Stream.eval(IO(queue.take())) ++ dequeueStream()
  }


}
