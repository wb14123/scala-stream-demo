package me.binwang.demo.stream

import cats.effect.IO

trait BaseStreamApp extends BaseApp {
  def produceStream(start: Int): fs2.Stream[IO, Int] = {
    if (start >= totalSize) {
      fs2.Stream.empty
    } else {
      fs2.Stream.evalSeq(produce(start, start + batchSize)) ++ produceStream(start + batchSize)
    }
  }
}
