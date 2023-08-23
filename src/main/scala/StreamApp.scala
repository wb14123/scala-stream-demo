package me.binwang.demo.stream

import cats.effect.IO

class StreamApp(config: TestConfig) extends TestRunner(config) {

  override val name = "pure stream"

  override def work(): IO[Unit] = {
    // add prefetch makes the performance comparable with StreamQueueApp
    produceStream(0).parEvalMap(config.batchSize)(consume).compile.drain
  }

}
