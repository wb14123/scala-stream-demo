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

  private val configs = Seq(
    new TestConfig {
      override val testName: String = "slow-producer"
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
      override val testName: String = "slow-consumer"
      override val produceDelay: FiniteDuration = 10.millis
      override val minConsumeDelayMillis: Long = 10
      override val maxConsumeDelayMillis: Long = 1000
    }
  )


  private def runNormalApp(configName: Option[String], runnerCls: Option[String]): IO[Unit] = {
    Blocker[IO].use { implicit blocker =>
      configs.filter(config => configName.isEmpty || config.testName.equals(configName.get)).map { config =>
        val runners = Seq(
          new BatchIOApp(config),
          new BlockingQueueApp(config),
          new StreamApp(config),
          new PrefetchStreamApp(config),
          new StreamQueueApp(config),
          new ConcurrentProducerApp(config),
          new ConcurrentProducerQueueApp(config),
        ).filter(runner => runnerCls.isEmpty || runner.getClass.getName.split('.').last.equals(runnerCls.get))
        for {
          _ <- asyncPrintln(s"=======================\nUsing setup: ${config.testName}")
          _ <- runners.map(_.run()).sequence
        } yield ()
      }.sequence.map(_ => ())
    }
  }

  private def runRealBlockingApp(): IO[Unit] = {
    Blocker[IO].use { implicit blocker =>
      val runner = new RealBlockingQueueApp(configs.head)
      for {
        _ <- asyncPrintln("This program will be blocked.")
        _ <- runner.run()
        _ <- asyncPrintln("If you see this message, then this program is not blocked")
      } yield ()
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    (if (args.size == 1 && args.head.equals("-n")) {
      runNormalApp(None, None)
    } else if (args.size == 2 && args.head.equals("-n")) {
      runNormalApp(None, args.get(1))
    } else if (args.size == 1 && args.head.equals("-b")) {
      runRealBlockingApp()
    } else {
      println(
        """
          |
          |Error to run.
          |
          |Usage:
          |
          |-n Run tests that will not block the whole program
          |-b Run RealBlockingQueueApp that will block the whole program
          |""".stripMargin)
      IO.pure()
    }).map(_ => ExitCode.Success)

  }
}
