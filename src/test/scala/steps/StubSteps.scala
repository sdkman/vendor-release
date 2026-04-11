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
package steps

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.Scenario
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.matchers.should.Matchers

class StubSteps extends ScalaDsl with EN with Matchers {

  And("""^the state API is available$""") { () =>
    stubFor(
      post(urlEqualTo("/login"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""{"token":"test-jwt-token"}""")
        )
    )
    stubFor(
      post(urlEqualTo("/versions"))
        .willReturn(
          aResponse()
            .withStatus(204)
        )
    )
  }

  And("""^the state API is unavailable$""") { () =>
    stubFor(
      post(urlEqualTo("/versions"))
        .willReturn(
          aResponse()
            .withStatus(500)
            .withBody("Internal Server Error")
        )
    )
  }

  And("""^the URI (.*) is available for download$""") { uri: String =>
    stubFor(
      get(urlEqualTo(uri))
        .willReturn(
          aResponse()
            .withHeader("content-type", "application/octet-stream")
            .withBodyFile(uri.tail)
            .withStatus(200)
        )
    )
  }

  And("""^the URI (.*) is not available for download$""") { uri: String =>
    stubFor(
      get(urlEqualTo(uri))
        .willReturn(aResponse().withStatus(404))
    )
  }

  Then("""^the state API received a POST request with platform (.*)$""") {
    expectedPlatform: String =>
      verify(
        postRequestedFor(urlEqualTo("/versions"))
          .withRequestBody(matchingJsonPath(s"$$[?(@.platform == '$expectedPlatform')]"))
      )
  }

  Then("""^the state API received a POST request with distribution (.*)$""") {
    expectedDistribution: String =>
      verify(
        postRequestedFor(urlEqualTo("/versions"))
          .withRequestBody(matchingJsonPath(s"$$[?(@.distribution == '$expectedDistribution')]"))
      )
  }

  Then("""^the state API received a POST request WITHOUT distribution""") { () =>
    verify(
      postRequestedFor(urlEqualTo("/versions"))
        .withRequestBody(matchingJsonPath(s"$$[?(!@.distribution)]"))
    )
  }

  Then("""^the state API received a POST request with version (.*)$""") { expectedVersion: String =>
    verify(
      postRequestedFor(urlEqualTo("/versions"))
        .withRequestBody(matchingJsonPath(s"$$[?(@.version == '$expectedVersion')]"))
    )
  }

  Then("""^the state API received a POST request with md5sum (.*)$""") { expectedMd5: String =>
    verify(
      postRequestedFor(urlEqualTo("/versions"))
        .withRequestBody(matchingJsonPath(s"$$[?(@.md5sum == '$expectedMd5')]"))
    )
  }

  Then("""^the state API received a POST request with sha256sum (.*)$""") {
    expectedSha256: String =>
      verify(
        postRequestedFor(urlEqualTo("/versions"))
          .withRequestBody(matchingJsonPath(s"$$[?(@.sha256sum == '$expectedSha256')]"))
      )
  }

  Then("""^the state API did not receive any POST requests$""") { () =>
    verify(0, postRequestedFor(urlEqualTo("/versions")))
  }

  Then("""^the state API received a POST request with a Bearer token$""") { () =>
    verify(
      postRequestedFor(urlEqualTo("/versions"))
        .withHeader("Authorization", matching("Bearer .*"))
    )
  }

  Then("""^the state API login endpoint was called (\d+) times?$""") { count: Int =>
    verify(count, postRequestedFor(urlEqualTo("/login")))
  }

  Then("""^the state API did not receive any POST requests to the versions endpoint$""") { () =>
    verify(0, postRequestedFor(urlEqualTo("/versions")))
  }

  And("""^the state API will return 401 on the first version request$""") { () =>
    stubFor(
      post(urlEqualTo("/login"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""{"token":"test-jwt-token"}""")
        )
    )
    stubFor(
      post(urlEqualTo("/versions"))
        .inScenario("token-expiry")
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(aResponse().withStatus(401))
        .willSetStateTo("re-authenticated")
    )
    stubFor(
      post(urlEqualTo("/versions"))
        .inScenario("token-expiry")
        .whenScenarioStateIs("re-authenticated")
        .willReturn(aResponse().withStatus(204))
    )
  }

  And("""^the state API login endpoint returns 401$""") { () =>
    stubFor(
      post(urlEqualTo("/login"))
        .willReturn(
          aResponse()
            .withStatus(401)
            .withBody("""{"error":"Invalid credentials"}""")
        )
    )
  }

  And("""^the state API login endpoint returns 429$""") { () =>
    stubFor(
      post(urlEqualTo("/login"))
        .willReturn(
          aResponse()
            .withStatus(429)
            .withBody("""{"error":"Rate limit exceeded"}""")
        )
    )
  }
}
