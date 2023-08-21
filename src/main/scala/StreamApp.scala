package me.binwang.demo.stream

import cats.effect.{ExitCode, IO}

object StreamApp extends BaseStreamApp {

  override def run(args: List[String]): IO[ExitCode] = {
    // add prefetch makes the performance comparable with StreamQueueApp
    val task = produceStream(0).prefetch.parEvalMap(batchSize)(consume).compile.drain
    printElapsedTime(task).map(_ => ExitCode.Success)
  }

}
