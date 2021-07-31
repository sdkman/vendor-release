name := "vendor-release"

val akkaHttpVersion = "10.2.4"
val scalaTestVersion = "3.1.0"
val scalajHttpVersion = "2.4.2"
val cucumberVersion = "2.0.1"

scalaVersion := "2.12.10"

val commonSettings = Seq(
  organization := "io.sdkman",
  scalaVersion := "2.12.10"
)

lazy val dockerSettings = Seq(
  dockerBaseImage := "openjdk:11",
  maintainer in Docker := "Marco Vermeulen <marco@sdkman.io>",
  dockerUpdateLatest := true,
  packageName := "sdkman/vendor-release"
)

parallelExecution in Test := false

resolvers ++= Seq(
  Resolver.mavenCentral,
  "jitpack" at "https://jitpack.io"
)

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "io.cucumber" %% "cucumber-scala" % cucumberVersion % Test,
  "io.cucumber" % "cucumber-junit" % cucumberVersion % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test,
  "org.scalaj" %% "scalaj-http" % scalajHttpVersion % Test,
  "com.github.tomakehurst" % "wiremock" % "2.2.2" % Test
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % "2.6.1",
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.8",
  "io.spray" %% "spray-json" % "1.3.2",
  "com.github.sdkman" % "sdkman-mongodb-persistence" % "1.9",
  "com.github.sdkman" % "sdkman-url-validator" % "0.2.4"
) ++ testDependencies

lazy val `vendor-release` = (project in file("."))
  .enablePlugins(DockerPlugin, JavaServerAppPackaging)
  .settings(commonSettings ++ dockerSettings: _*)

import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(releaseStepTask(publish in Docker)),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
