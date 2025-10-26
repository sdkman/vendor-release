package io.sdkman.vendor.release.routes

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer, SystemMaterializer}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
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
  }

  trait TestConfiguration extends Configuration {
    override lazy val stateApiUrl: String               = "http://localhost:8089"
    override lazy val stateApiBasicAuthUsername: String = "testuser"
    override lazy val stateApiBasicAuthPassword: String = "testpass"
  }

  class TestClient extends HttpStateApiClient with TestConfiguration {
    override implicit val actorSystem: ActorSystem = HttpStateApiClientSpec.this.actorSystem
  }

  lazy val client = new TestClient()

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
          .withRequestBody(matchingJsonPath("$[?(@.vendor == 'tem')]"))
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
          .withRequestBody(matchingJsonPath("$[?(!@.vendor)]"))
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
