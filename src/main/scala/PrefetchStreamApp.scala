package me.binwang.demo.stream

import cats.effect.IO

class PrefetchStreamApp(config: TestConfig) extends TestRunner(config) {

  override val name = "pure stream with prefetch"

  override def work(): IO[Unit] = {
    // add prefetch makes the performance comparable with StreamQueueApp
    produceStream(0).prefetch.parEvalMap(config.batchSize)(consume).compile.drain
  }

}
