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
package steps

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.typesafe.scalalogging.LazyLogging
import cucumber.api.scala.ScalaDsl
import io.sdkman.vendor.release.HttpServer
import support.{Mongo, Postgres}

class Env extends ScalaDsl with LazyLogging {

  val wireMockServer = new WireMockServer(options().port(8080))
  wireMockServer.start()

  val app = new HttpServer()
  app.migrate()
  app.start()

  sys.addShutdownHook {
    logger.info("Shutting down test server...")
    app.shutdown()
    wireMockServer.stop()
  }

  Before() { s =>
    Mongo.dropAllCollections()
    Postgres.truncateVersion()
    WireMock.reset()
    World.reset()
  }
}
