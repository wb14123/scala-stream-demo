package me.binwang.demo.stream

import cats.effect.{ContextShift, IO, IOApp, Timer}

import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, DurationLong, FiniteDuration}
import scala.util.Random

trait BaseApp extends IOApp {

  private val executor = Executors.newFixedThreadPool(2, (r: Runnable) => {
    val back = new Thread(r)
    back.setDaemon(true)
    back
  })
  override implicit def executionContext: ExecutionContext = ExecutionContext.fromExecutor(executor)
  override implicit def timer: Timer[IO] = IO.timer(executionContext)
  override implicit def contextShift: ContextShift[IO] = IO.contextShift(executionContext)

  protected val produceDelay: FiniteDuration = 1000.millis
  protected val minConsumeDelayMills: Long = 10
  protected val maxConsumeDelayMills: Long = 2000

  protected val batchSize = 100
  protected val totalSize = 5000


  def produce(start: Int, end: Int): IO[Seq[Int]] = {
    for {
      _ <- IO(println(s"Generating from $start to $end"))
      _ <- IO.sleep(produceDelay)
      _ <- IO(println(s"Generated from $start to $end"))
      result = Range(start, end)
    } yield result
  }

  protected def consume(x: Int): IO[Unit] = {
    val consumeDelayMillis = Random.nextLong(maxConsumeDelayMills - minConsumeDelayMills) + minConsumeDelayMills
    val consumeDelay = consumeDelayMillis.millis
    for {
      _ <- IO.sleep(consumeDelay)
      _ <- IO(println(s"Consumed $x, took $consumeDelayMillis ms"))
    } yield ()
  }

  protected def printElapsedTime(task: IO[_]): IO[Unit] = {
    for {
      start <- IO(System.nanoTime())
      _ <- task
      end <- IO(System.nanoTime())
      timeDiff = (end - start).nanos
      _ <- IO(println(s"Time used: ${timeDiff.toUnit(TimeUnit.MILLISECONDS)} ms"))
    } yield ()

  }

}
