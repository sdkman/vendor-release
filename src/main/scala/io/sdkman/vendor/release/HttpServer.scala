/**
  * Copyright 2023 SDKMAN!
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
import akka.http.scaladsl.server.Route
import io.sdkman.vendor.release.routes.{
  CandidateReleaseRoutes,
  VersionDefaultRoutes,
  HealthRoutes,
  VersionReleaseRoutes
}
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class HttpServer
    extends Configuration
    with VersionReleaseRoutes
    with CandidateReleaseRoutes
    with VersionDefaultRoutes
    with HealthRoutes {

  implicit lazy val actorSystem: ActorSystem = ActorSystem("vendor-release-service")

  val routes
      : Route = healthRoutes ~ versionReleaseRoutes ~ versionDefaultRoutes ~ candidateReleaseRoutes

  private val flyway = Flyway
    .configure()
    .dataSource(jdbcUrl, jdbcUser, jdbcPassword)
    .load()

  def migrate(): MigrateResult = flyway.migrate()

  def start(): Future[Http.ServerBinding] =
    Http().newServerAt(serviceHost, servicePort).bindFlow(routes)

  def shutdown(): Unit = {
    Await.ready(actorSystem.terminate(), 5.seconds)
  }
}

object HttpServer extends App {
  val server = new HttpServer
  server.migrate()
  server.start()
}
