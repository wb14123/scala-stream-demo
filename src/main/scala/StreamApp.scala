package me.binwang.demo.stream

import cats.effect.{ExitCode, IO}

object StreamApp extends BaseStreamApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val task = produceStream(0).parEvalMap(batchSize)(consume).compile.drain
    printElapsedTime(task).map(_ => ExitCode.Success)
  }

}
