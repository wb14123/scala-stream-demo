package me.binwang.demo.stream

import cats.effect.IO
import cats.implicits._

class BatchIOApp(config: TestConfig) extends TestRunner(config) {

  override val name = "IO batch"

  override def work(): IO[Unit] = {
    loop(0)
  }

  private def loop(start: Int): IO[Unit] = {
    if (start >= config.totalSize) {
      IO.unit
    } else {
      produce(start, start + config.batchSize)
        .flatMap {_.map(consume).parSequence}
        .flatMap(_ => loop(start + config.batchSize))
    }
  }

}
