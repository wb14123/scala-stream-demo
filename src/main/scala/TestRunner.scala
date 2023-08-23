package me.binwang.demo.stream

import AsyncConsole.{asyncPrint, asyncPrintln}

import cats.effect.{Blocker, ContextShift, IO, Timer}

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration.DurationLong
import scala.util.Random


abstract class TestRunner(val config: TestConfig)(
  implicit val timer: Timer[IO], implicit val contextShift: ContextShift[IO], implicit val blocker: Blocker)  {

  val name: String
  def work(): IO[Unit]


  // do not compress produce output
  private val printRatio = Math.ceil(config.totalSize / (config.progressBarWidth - config.totalSize / config.batchSize).toDouble).toInt
  private val consumeCounter = new AtomicInteger(0)

  def run(): IO[Unit] = {
    val task = work()
    printElapsedTime(task)
  }

  def produce(start: Int, end: Int): IO[Seq[Int]] = {
    for {
      _ <- IO.sleep(config.produceDelay)
      // do not compress produce output
      _ <-  asyncPrint("P")
      result = Range(start, end)
    } yield result
  }

  protected def consume(x: Int): IO[Unit] = {
    val consumeDelayMillis = Random.nextLong(config.maxConsumeDelayMillis - config.minConsumeDelayMillis) + config.minConsumeDelayMillis
    val consumeDelay = consumeDelayMillis.millis
    for {
      _ <- IO.sleep(consumeDelay)
      // compress consumer print to fit in one line of output
      _ <- if (consumeCounter.getAndIncrement() % printRatio == 0) asyncPrint("C") else IO.pure()
    } yield ()
  }

  protected def printElapsedTime(task: IO[_]): IO[Unit] = {
    for {
      _ <- asyncPrintln(s"Testing runner: $name")
      start <- IO(System.nanoTime())
      _ <- task
      end <- IO(System.nanoTime())
      timeDiff = (end - start).nanos
      _ <- asyncPrintln(s"\nTime used: ${timeDiff.toUnit(TimeUnit.MILLISECONDS)} ms")
    } yield ()

  }

  protected def produceStream(start: Int, end: Int = config.totalSize): fs2.Stream[IO, Int] = {
    if (start >= end) {
      fs2.Stream.empty
    } else {
      fs2.Stream.evalSeq(produce(start, start + config.batchSize)) ++ produceStream(start + config.batchSize, end)
    }
  }

}
