package me.binwang.demo.stream

import cats.effect.{ExitCode, IO}
import cats.implicits._

object BatchIOApp extends BaseApp {

  private val size = 10

  override def run(args: List[String]): IO[ExitCode] = {
    loop(0).map(_ => ExitCode.Success)
  }

  private def loop(start: Int): IO[Unit] = {
    produce(start, start + size)
      .flatMap { _.map(consume).parSequence}
      .flatMap(_ => loop(start + size))
  }

}
