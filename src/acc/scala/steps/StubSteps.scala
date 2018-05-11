/**
  * Copyright 2018 Marco Vermeulen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package steps

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers


class StubSteps extends ScalaDsl with EN with Matchers {

  val WiremockHost = "wiremock"

  val WiremockPort = 8080

  Before() { s =>
    WireMock.configureFor(WiremockHost, WiremockPort)
  }

  And( """^the URI (.*) is available for download$""") { uri: String =>

    stubFor(get(urlEqualTo(uri)).willReturn(
      aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/zip")
        .withBodyFile(uri.substring(1))))
  }

  And( """^the URI (.*) is not available for download$""") { uri: String =>
    stubFor(get(urlEqualTo(uri))
      .willReturn(aResponse().withStatus(404)))
  }
}
