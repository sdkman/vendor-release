/**
  * Copyright 2018 Marco Vermeulen
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
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import cucumber.api.scala.ScalaDsl
import support.Mongo

class Env extends ScalaDsl {

  val WireMockHost = "localhost"

  val WireMockPort = 8080

  configureFor(WireMockHost, WireMockPort)

  lazy val wireMockServer = new WireMockServer(wireMockConfig().port(WireMockPort))
  wireMockServer.start()

  Before() { s =>
    Mongo.dropAllCollections()
    WireMock.reset()
  }
}
