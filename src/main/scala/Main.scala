package me.binwang.demo.stream

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    val configs = Seq(
      new TestConfig {
        override val testName: String = "slow producer"
        override val produceDelay: FiniteDuration = 1000.millis
        override val minConsumeDelayMillis: Long = 10
        override val maxConsumeDelayMillis: Long = 1000
      },
      new TestConfig {
        override val testName: String = "balanced"
        override val produceDelay: FiniteDuration = 1005.millis
        override val minConsumeDelayMillis: Long = 10
        override val maxConsumeDelayMillis: Long = 2000
      },
      new TestConfig {
        override val testName: String = "slow consumer"
        override val produceDelay: FiniteDuration = 10.millis
        override val minConsumeDelayMillis: Long = 10
        override val maxConsumeDelayMillis: Long = 1000
      }
    )

    configs.map { config =>
      val runners = Seq(
        new BatchIOApp(config),
        new StreamApp(config),
        new PrefetchStreamApp(config),
        new StreamQueueApp(config),
      )
      for {
        _ <- IO(println(s"========================"))
        _ <- IO(println(s"Using setup: ${config.testName}"))
        _ <- runners.map(_.run()).sequence
      } yield ()
    }.sequence.map(_ => ExitCode.Success)


  }
}
