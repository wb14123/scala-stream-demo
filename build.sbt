ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "scala-stream-demo",
  )


libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "2.5.10",
)