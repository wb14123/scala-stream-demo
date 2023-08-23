package me.binwang.demo.stream

import cats.effect.{Blocker, ContextShift, IO}

object AsyncConsole {

  def asyncPrint(s: String)(implicit cs: ContextShift[IO], blocker: Blocker): IO[Unit] = blocker.blockOn(IO(print(s)))
  def asyncPrintln(s: String)(implicit cs: ContextShift[IO], blocker: Blocker): IO[Unit] = blocker.blockOn(IO(println(s)))

}
