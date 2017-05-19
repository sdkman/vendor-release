package io.sdkman.vendor.release

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.{Await, Future}

import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

class HttpServer extends Configuration with ApiRoutes {

  implicit lazy val actorSystem = ActorSystem("vendor-release-service")
  implicit lazy val materializer = ActorMaterializer()


  def start() : Future[Seq[Http.ServerBinding]] = Future.sequence {
    Seq(
      Http().bindAndHandle(apiRoute, serviceHost, servicePort)
    )
  }

  def shutdown(): Unit = {
    Await.ready(actorSystem.terminate(), 5.seconds)
  }
}

object HttpServer extends App {
  val server = new HttpServer
  server.start()

}
