import scala.io.Source

name := "vendor-release"

val akkaHttpVersion = "10.0.2"
val scalaTestVersion = "3.0.1"
val scalajHttpVersion = "2.3.0"
val cucumberVersion = "2.0.1"

scalaVersion := "2.12.8"

val commonSettings = Seq(
  organization := "io.sdkman",
  scalaVersion := "2.12.8"
)

lazy val dockerSettings = Seq(
  dockerBaseImage := "openjdk:11",
  maintainer in Docker := "Marco Vermeulen <marco@sdkman.io>",
  dockerUpdateLatest := true,
  packageName := "sdkman/vendor-release"
)

lazy val AcceptanceTest = config("acc") extend IntegrationTest
parallelExecution in AcceptanceTest := false

resolvers ++= Seq(
  Resolver.bintrayRepo("sdkman", "maven"),
  Resolver.jcenterRepo
)

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test
)

val accDependencies = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % AcceptanceTest,
  "io.cucumber" %% "cucumber-scala" % cucumberVersion % AcceptanceTest,
  "io.cucumber" % "cucumber-junit" % cucumberVersion % AcceptanceTest,
  "com.novocode" % "junit-interface" % "0.11" % AcceptanceTest,
  "org.scalaj" %% "scalaj-http" % scalajHttpVersion % AcceptanceTest,
  "com.github.tomakehurst" % "wiremock" % "2.2.2" % AcceptanceTest
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.16",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.8",
  "io.spray" %% "spray-json" % "1.3.2",
  "io.sdkman" %% "sdkman-mongodb-persistence" % "1.1",
  "io.sdkman" %% "sdkman-url-validator" % "0.2.4"
) ++ testDependencies ++ accDependencies

lazy val `vendor-release` = (project in file("."))
  .enablePlugins(DockerPlugin, JavaServerAppPackaging)
  .configs(IntegrationTest)
  .configs(AcceptanceTest)
  .settings(inConfig(AcceptanceTest)(Defaults.testSettings): _*)
  .settings(commonSettings ++ dockerSettings: _*)

import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,              // : ReleaseStep
  inquireVersions,                        // : ReleaseStep
  runClean,                               // : ReleaseStep
  setReleaseVersion,                      // : ReleaseStep
  commitReleaseVersion,                   // : ReleaseStep, performs the initial git checks
  tagRelease,                             // : ReleaseStep
  ReleaseStep(releaseStepTask(publish in Docker)), // : ReleaseStep, checks whether `publishTo` is properly set up
  setNextVersion,                         // : ReleaseStep
  commitNextVersion,                      // : ReleaseStep
  pushChanges                             // : ReleaseStep, also checks that an upstream branch is properly configured
)
