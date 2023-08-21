package me.binwang.demo.stream

import cats.effect.{ExitCode, IO}
import cats.implicits._

object BatchIOApp extends BaseApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val task = loop(0)
    printElapsedTime(task).map(_ => ExitCode.Success)
  }

  private def loop(start: Int): IO[Unit] = {
    if (start >= totalSize) {
      IO.unit
    } else {
      produce(start, start + batchSize)
        .flatMap {_.map(consume).parSequence}
        .flatMap(_ => loop(start + batchSize))
    }
  }

}
