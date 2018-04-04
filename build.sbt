import scala.io.Source

name := "vendor-release"

val akkaHttpVersion = "10.0.2"
val scalaTestVersion = "3.0.1"
val scalajHttpVersion = "2.3.0"
val cucumberVersion = "1.2.5"

scalaVersion := "2.11.8"

val commonSettings = Seq(
  organization := "io.sdkman",
  scalaVersion := "2.11.8"
)

version := Source.fromFile("version").getLines.mkString

lazy val dockerSettings = Seq(
  dockerBaseImage := "openjdk:8",
  maintainer in Docker := "Marco Vermeulen <marco@sdkman.io>",
  dockerUpdateLatest := true,
  packageName := "sdkman/vendor-release"
)

lazy val IntegrationTest = config("it") extend Test

lazy val itSettings = Defaults.itSettings ++ Seq(
  parallelExecution in IntegrationTest := false,
  testOptions in IntegrationTest += Tests.Argument(
    TestFrameworks.ScalaTest, "-F", Option(System.getProperty("itTimeScale")).getOrElse("1"))
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

val itDependencies = Seq()

val accDependencies = Seq(
  "org.scalatest" %% "scalatest" % scalaTestVersion % AcceptanceTest,
  "info.cukes" %% "cucumber-scala" % cucumberVersion % AcceptanceTest,
  "info.cukes" % "cucumber-junit" % cucumberVersion % AcceptanceTest,
  "com.novocode" % "junit-interface" % "0.11" % AcceptanceTest,
  "org.scalaj" %% "scalaj-http" % scalajHttpVersion % AcceptanceTest
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.16",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.8",
  "io.spray" %% "spray-json" % "1.3.2",
  "io.sdkman" %% "sdkman-mongodb-persistence" % "0.9"
) ++ testDependencies ++ itDependencies ++ accDependencies

lazy val `vendor-release` = (project in file("."))
  .enablePlugins(DockerPlugin, JavaServerAppPackaging)
  .configs(IntegrationTest)
  .configs(AcceptanceTest)
  .settings(inConfig(AcceptanceTest)(Defaults.testSettings): _*)
  .settings(commonSettings ++ dockerSettings ++ itSettings: _*)
