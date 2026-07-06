package io.sdkman.vendor.release.routes

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer, SystemMaterializer}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import io.sdkman.model.Version
import io.sdkman.vendor.release.Configuration
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class HttpStateApiClientSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfterAll
    with BeforeAndAfterEach {

  implicit val patience: PatienceConfig           = PatienceConfig(timeout = 5.seconds)
  implicit val actorSystem: ActorSystem           = ActorSystem()
  implicit val materializer: Materializer         = SystemMaterializer(actorSystem).materializer
  implicit val executionContext: ExecutionContext = actorSystem.dispatcher

  val wireMockServer = new WireMockServer(wireMockConfig().port(8089))

  override def beforeAll(): Unit = {
    wireMockServer.start()
    configureFor("localhost", 8089)
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()
    actorSystem.terminate()
  }

  override def beforeEach(): Unit = {
    wireMockServer.resetAll()
    client.cachedToken.set(None)
    stubFor(
      post(urlEqualTo("/login"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""{"token":"test-jwt-token"}""")
        )
    )
  }

  trait TestConfiguration extends Configuration {
    override lazy val stateApiUrl: String      = "http://localhost:8089"
    override lazy val stateApiEmail: String    = "testuser@test.com"
    override lazy val stateApiPassword: String = "testpass"
  }

  class TestClient extends HttpStateApiClient with TestConfiguration

  val client = new TestClient()

  "HttpStateApiClient" should {

    "transform Version with platform mapping to StateVersion" in {
      stubFor(
        post(urlEqualTo("/versions"))
          .willReturn(aResponse().withStatus(204))
      )

      val version = Version(
        candidate = "java",
        version = "17.0.1",
        platform = "MAC_OSX",
        url = "http://example.com/java.zip",
        vendor = Some("tem"),
        visible = Some(true),
        checksums = Some(Map("MD5" -> "abc123", "SHA-256" -> "def456"))
      )

      client.upsertVersionStateApi(version).futureValue

      verify(
        postRequestedFor(urlEqualTo("/versions"))
          .withRequestBody(matchingJsonPath("$[?(@.version == '17.0.1')]"))
          .withRequestBody(matchingJsonPath("$[?(@.platform == 'MAC_X64')]"))
          .withRequestBody(matchingJsonPath("$[?(@.distribution == 'TEMURIN')]"))
          .withRequestBody(matchingJsonPath("$[?(@.md5sum == 'abc123')]"))
          .withRequestBody(matchingJsonPath("$[?(@.sha256sum == 'def456')]"))
      )
    }

    "use NO vendor when vendor is None" in {
      stubFor(
        post(urlEqualTo("/versions"))
          .willReturn(aResponse().withStatus(204))
      )

      val version = Version(
        candidate = "java",
        version = "17.0.1",
        platform = "UNIVERSAL",
        url = "http://example.com/java.zip",
        vendor = None,
        visible = Some(true)
      )

      client.upsertVersionStateApi(version).futureValue

      verify(
        postRequestedFor(urlEqualTo("/versions"))
          .withRequestBody(matchingJsonPath("$[?(!@.distribution)]"))
      )
    }

    "extract checksums from various algorithm formats" in {
      stubFor(
        post(urlEqualTo("/versions"))
          .willReturn(aResponse().withStatus(204))
      )

      val version = Version(
        candidate = "java",
        version = "17.0.1",
        platform = "UNIVERSAL",
        url = "http://example.com/java.zip",
        checksums = Some(
          Map(
            "md5"     -> "lowercase-md5",
            "SHA-256" -> "uppercase-sha256",
            "SHA512"  -> "sha512-value"
          )
        )
      )

      client.upsertVersionStateApi(version).futureValue

      verify(
        postRequestedFor(urlEqualTo("/versions"))
          .withRequestBody(matchingJsonPath("$[?(@.md5sum == 'lowercase-md5')]"))
          .withRequestBody(matchingJsonPath("$[?(@.sha256sum == 'uppercase-sha256')]"))
          .withRequestBody(matchingJsonPath("$[?(@.sha512sum == 'sha512-value')]"))
      )
    }

    "handle missing checksums gracefully" in {
      stubFor(
        post(urlEqualTo("/versions"))
          .willReturn(aResponse().withStatus(204))
      )

      val version = Version(
        candidate = "java",
        version = "17.0.1",
        platform = "UNIVERSAL",
        url = "http://example.com/java.zip",
        checksums = None
      )

      client.upsertVersionStateApi(version).futureValue

      verify(
        postRequestedFor(urlEqualTo("/versions"))
          .withRequestBody(matchingJsonPath("$[?(!@.md5sum)]"))
          .withRequestBody(matchingJsonPath("$[?(!@.sha256sum)]"))
          .withRequestBody(matchingJsonPath("$[?(!@.sha512sum)]"))
      )
    }

    "omit the tags field when serialising a StateVersion with tags None" in {
      import VersionJsonProtocol._
      import spray.json._

      val stateVersion = StateVersion(
        candidate = "groovy",
        version = "2.3.6",
        distribution = None,
        url = "http://example.com/groovy.zip",
        tags = None
      )

      val json = stateVersion.toJson.asJsObject
      json.fields.keySet should not contain "tags"
    }

    "serialise tags as [\"lts\"] when a StateVersion carries the lts tag" in {
      import VersionJsonProtocol._
      import spray.json._

      val stateVersion = StateVersion(
        candidate = "groovy",
        version = "2.3.6",
        distribution = None,
        url = "http://example.com/groovy.zip",
        tags = Some(List(HttpStateApiClient.LtsTag))
      )

      val json = stateVersion.toJson.asJsObject
      json.fields("tags") shouldBe JsArray(JsString("lts"))
    }

    "post tags [\"lts\"] to /versions when upserting with the lts tag" in {
      stubFor(
        post(urlEqualTo("/versions"))
          .willReturn(aResponse().withStatus(204))
      )

      val version = Version(
        candidate = "groovy",
        version = "2.3.6",
        platform = "UNIVERSAL",
        url = "http://example.com/groovy.zip",
        vendor = None,
        visible = Some(true)
      )

      client.upsertVersionStateApi(version, Some(List(HttpStateApiClient.LtsTag))).futureValue

      verify(
        postRequestedFor(urlEqualTo("/versions"))
          .withRequestBody(matchingJsonPath("$[?(@.tags[0] == 'lts')]"))
      )
    }

    "post no tags field to /versions when upserting without tags" in {
      stubFor(
        post(urlEqualTo("/versions"))
          .willReturn(aResponse().withStatus(204))
      )

      val version = Version(
        candidate = "groovy",
        version = "2.3.6",
        platform = "UNIVERSAL",
        url = "http://example.com/groovy.zip",
        vendor = None,
        visible = Some(true)
      )

      client.upsertVersionStateApi(version, None).futureValue

      verify(
        postRequestedFor(urlEqualTo("/versions"))
          .withRequestBody(matchingJsonPath("$[?(!@.tags)]"))
      )
    }

    "post a tag assignment to /versions/tags with a Bearer token and correct body" in {
      stubFor(
        post(urlEqualTo("/versions/tags"))
          .willReturn(aResponse().withStatus(204))
      )

      client
        .assignTagStateApi("groovy", "2.3.6", None, "UNIVERSAL", HttpStateApiClient.LtsTag)
        .futureValue

      verify(
        postRequestedFor(urlEqualTo("/versions/tags"))
          .withHeader("Authorization", equalTo("Bearer test-jwt-token"))
          .withRequestBody(matchingJsonPath("$[?(@.candidate == 'groovy')]"))
          .withRequestBody(matchingJsonPath("$[?(@.version == '2.3.6')]"))
          .withRequestBody(matchingJsonPath("$[?(@.platform == 'UNIVERSAL')]"))
          .withRequestBody(matchingJsonPath("$[?(@.tag == 'lts')]"))
          .withRequestBody(matchingJsonPath("$[?(!@.distribution)]"))
      )
    }

    "include the distribution in the tag body when it is present" in {
      stubFor(
        post(urlEqualTo("/versions/tags"))
          .willReturn(aResponse().withStatus(204))
      )

      client
        .assignTagStateApi(
          "java",
          "17.0.1",
          Some("TEMURIN"),
          "LINUX_X64",
          HttpStateApiClient.LtsTag
        )
        .futureValue

      verify(
        postRequestedFor(urlEqualTo("/versions/tags"))
          .withRequestBody(matchingJsonPath("$[?(@.distribution == 'TEMURIN')]"))
      )
    }

    "re-authenticate once and retry the tag write on a 401" in {
      stubFor(
        post(urlEqualTo("/versions/tags"))
          .inScenario("tag-401")
          .whenScenarioStateIs(STARTED)
          .willReturn(aResponse().withStatus(401))
          .willSetStateTo("retried")
      )
      stubFor(
        post(urlEqualTo("/versions/tags"))
          .inScenario("tag-401")
          .whenScenarioStateIs("retried")
          .willReturn(aResponse().withStatus(204))
      )

      client
        .assignTagStateApi("groovy", "2.3.6", None, "UNIVERSAL", HttpStateApiClient.LtsTag)
        .futureValue

      verify(2, postRequestedFor(urlEqualTo("/versions/tags")))
      verify(2, postRequestedFor(urlEqualTo("/login")))
    }

    "recover a 404 tag write to Future.unit via bestEffortNonJava" in {
      stubFor(
        post(urlEqualTo("/versions/tags"))
          .willReturn(aResponse().withStatus(404))
      )

      client
        .bestEffortNonJava("groovy")(
          client.assignTagStateApi("groovy", "2.3.6", None, "UNIVERSAL", HttpStateApiClient.LtsTag)
        )
        .futureValue shouldBe (())

      verify(postRequestedFor(urlEqualTo("/versions/tags")))
    }

    "recover a 5xx tag write to Future.unit via bestEffortNonJava" in {
      stubFor(
        post(urlEqualTo("/versions/tags"))
          .willReturn(aResponse().withStatus(503))
      )

      client
        .bestEffortNonJava("groovy")(
          client.assignTagStateApi("groovy", "2.3.6", None, "UNIVERSAL", HttpStateApiClient.LtsTag)
        )
        .futureValue shouldBe (())
    }

    "recover a connection failure to Future.unit via bestEffortNonJava" in {
      stubFor(
        post(urlEqualTo("/versions/tags"))
          .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE))
      )

      client
        .bestEffortNonJava("groovy")(
          client.assignTagStateApi("groovy", "2.3.6", None, "UNIVERSAL", HttpStateApiClient.LtsTag)
        )
        .futureValue shouldBe (())
    }

    "skip the State API entirely for java candidates via bestEffortNonJava" in {
      client
        .bestEffortNonJava("java")(
          client.assignTagStateApi(
            "java",
            "17.0.1",
            Some("TEMURIN"),
            "LINUX_X64",
            HttpStateApiClient.LtsTag
          )
        )
        .futureValue shouldBe (())

      verify(0, postRequestedFor(urlEqualTo("/versions/tags")))
    }

    "fail with meaningful error when state API returns error" in {
      stubFor(
        post(urlEqualTo("/versions"))
          .willReturn(
            aResponse()
              .withStatus(400)
              .withBody("{\"error\": \"Invalid platform\"}")
          )
      )

      val version = Version(
        candidate = "java",
        version = "17.0.1",
        platform = "UNIVERSAL",
        url = "http://example.com/java.zip"
      )

      val failure = client.upsertVersionStateApi(version).failed.futureValue
      failure.getMessage should include("400")
      failure.getMessage should include("Invalid platform")
    }
  }
}
