package me.binwang.demo.stream

import AsyncConsole.asyncPrintln

import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp, Timer}
import cats.implicits._

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object Main extends IOApp {

  private val executor = Executors.newFixedThreadPool(2, (r: Runnable) => {
    val back = new Thread(r)
    back.setDaemon(true)
    back
  })

  implicit override def executionContext: ExecutionContext = ExecutionContext.fromExecutor(executor)

  implicit override def timer: Timer[IO] = IO.timer(executionContext)

  implicit override def contextShift: ContextShift[IO] = IO.contextShift(executionContext)

  override def run(args: List[String]): IO[ExitCode] = {

    val configs = Seq(
      new TestConfig {
        override val testName: String = "slow producer"
        override val produceDelay: FiniteDuration = 1000.millis
        override val minConsumeDelayMillis: Long = 10
        override val maxConsumeDelayMillis: Long = 100
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

    Blocker[IO].use { implicit blocker =>
      configs.map { config =>
        val runners = Seq(
          new BatchIOApp(config),
          new BlockingQueueApp(config),
          new StreamApp(config),
          new PrefetchStreamApp(config),
          new StreamQueueApp(config),
        )
        for {
          _ <- asyncPrintln(s"=======================\nUsing setup: ${config.testName}")
          _ <- runners.map(_.run()).sequence
        } yield ()
      }.sequence.map(_ => ExitCode.Success)
    }


  }
}
