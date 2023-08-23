package me.binwang.demo.stream

import scala.concurrent.duration.FiniteDuration

trait TestConfig {
  val testName: String
  val produceDelay: FiniteDuration
  val minConsumeDelayMillis: Long
  val maxConsumeDelayMillis: Long
  val batchSize = 100
  val totalSize = 1000
  val threads = 2
  val progressBarWidth: Int = Integer.parseInt(sys.env.getOrElse("COLUMNS", "80"))
}

