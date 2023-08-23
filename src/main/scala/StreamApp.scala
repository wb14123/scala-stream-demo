package me.binwang.demo.stream

import cats.effect.{ContextShift, IO, Timer}

class StreamApp(config: TestConfig)(implicit timer: Timer[IO], contextShift: ContextShift[IO]) extends TestRunner(config) {

  override val name = "pure stream"

  override def work(): IO[Unit] = {
    // add prefetch makes the performance comparable with StreamQueueApp
    produceStream(0).parEvalMap(config.batchSize)(consume).compile.drain
  }

}
