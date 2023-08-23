package me.binwang.demo.stream

import cats.effect.{ContextShift, IO, Timer}

import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationLong
import scala.util.Random


abstract class TestRunner(val config: TestConfig)  {

  val name: String
  def work(): IO[Unit]

  private val executor = Executors.newFixedThreadPool(config.threads, (r: Runnable) => {
    val back = new Thread(r)
    back.setDaemon(true)
    back
  })
  implicit def executionContext: ExecutionContext = ExecutionContext.fromExecutor(executor)
  implicit def timer: Timer[IO] = IO.timer(executionContext)
  implicit def contextShift: ContextShift[IO] = IO.contextShift(executionContext)


  def run(): IO[Unit] = {
    val task = work()
    printElapsedTime(task)
  }

  def produce(start: Int, end: Int): IO[Seq[Int]] = {
    for {
      // _ <- IO(println(s"Generating from $start to $end"))
      _ <- IO.sleep(config.produceDelay)
      // _ <- IO(println(s"Generated from $start to $end"))
      _ <- IO(print("G"))
      result = Range(start, end)
    } yield result
  }

  protected def consume(x: Int): IO[Unit] = {
    val consumeDelayMillis = Random.nextLong(config.maxConsumeDelayMillis - config.minConsumeDelayMillis) + config.minConsumeDelayMillis
    val consumeDelay = consumeDelayMillis.millis
    for {
      _ <- IO.sleep(consumeDelay)
      // _ <- IO(println(s"Consumed $x, took $consumeDelayMillis ms"))
      _ <- IO(print("C"))
    } yield ()
  }

  protected def printElapsedTime(task: IO[_]): IO[Unit] = {
    for {
      _ <- IO(println(s"Testing runner: $name"))
      start <- IO(System.nanoTime())
      _ <- task
      end <- IO(System.nanoTime())
      timeDiff = (end - start).nanos
      _ <- IO(println())
      _ <- IO(println(s"Time used: ${timeDiff.toUnit(TimeUnit.MILLISECONDS)} ms"))
    } yield ()

  }

  protected def produceStream(start: Int): fs2.Stream[IO, Int] = {
    if (start >= config.totalSize) {
      fs2.Stream.empty
    } else {
      fs2.Stream.evalSeq(produce(start, start + config.batchSize)) ++ produceStream(start + config.batchSize)
    }
  }

}
