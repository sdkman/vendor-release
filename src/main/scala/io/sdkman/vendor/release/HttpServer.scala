/**
  * Copyright 2020 Marco Vermeulen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package io.sdkman.vendor.release

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import io.sdkman.vendor.release.routes.{DefaultRoutes, HealthRoutes, ReleaseRoutes}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class HttpServer extends Configuration with ReleaseRoutes with DefaultRoutes with HealthRoutes {

  implicit lazy val actorSystem = ActorSystem("vendor-release-service")

  val routes = healthRoutes ~ releaseRoutes ~ defaultRoutes

  def start(): Future[Http.ServerBinding] = Http().bindAndHandle(routes, serviceHost, servicePort)

  def shutdown(): Unit = {
    Await.ready(actorSystem.terminate(), 5.seconds)
  }
}

object HttpServer extends App {
  val server = new HttpServer
  server.start()
}
