package me.binwang.demo.stream

import cats.effect.{Blocker, ContextShift, IO, Timer}

class ConcurrentProducerApp(config: TestConfig)(
  implicit timer: Timer[IO], contextShift: ContextShift[IO], blocker: Blocker) extends TestRunner(config) {

  override val name = "stream with fs2 queue with concurrent producer"

  private val produceParallelism = 2

  override def work(): IO[Unit] = {
    fs2.Stream.emits(Range(0, produceParallelism))
      .map(batch => produceStream(
        batch * config.totalSize / produceParallelism,
        (batch + 1) * config.totalSize / produceParallelism.toDouble))
      .parJoin(produceParallelism)
      .prefetch
      .parEvalMap(config.batchSize)(consume).compile.drain
  }


}
