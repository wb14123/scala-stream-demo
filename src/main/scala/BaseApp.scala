package me.binwang.demo.stream

import cats.effect.{IO, IOApp}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{DurationInt, DurationLong, FiniteDuration}

trait BaseApp extends IOApp {

  protected val produceDelay: FiniteDuration = 1.seconds
  protected val consumeDelay: FiniteDuration = 10.millis


  def produce(start: Int, end: Int): IO[Seq[Int]] = {
    for {
      _ <- IO(println(s"Generating from $start to $end"))
      _ <- IO.sleep(produceDelay)
      _ <- IO(println(s"Generated from $start to $end"))
      result = Range(start, end)
    } yield result
  }

  def produceAsStream(start: Int, size: Int): fs2.Stream[IO, Int] = {
    fs2.Stream.evalSeq(produce(start, size))
  }

  protected def consume(x: Int): IO[Unit] = {
    for {
      _ <- IO.sleep(consumeDelay)
      _ <- IO(println(s"Consumed $x"))
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
