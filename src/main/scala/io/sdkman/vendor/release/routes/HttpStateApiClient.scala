package io.sdkman.vendor.release.routes

import spray.json.DefaultJsonProtocol
import io.sdkman.vendor.release.Configuration
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import io.sdkman.model.Version
import scala.concurrent.Future
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._

import scala.concurrent.ExecutionContext.Implicits.global

case class StateVersion(
    candidate: String,
    version: String,
    vendor: String,
    url: String,
    platform: String = "UNIVERSAL",
    visible: Boolean = true,
    md5sum: Option[String] = None,
    sha256sum: Option[String] = None,
    sha512sum: Option[String] = None
)

object VersionJsonProtocol extends DefaultJsonProtocol {
  implicit val versionFormat = jsonFormat9(StateVersion)
}

trait HttpStateApiClient {
  self: Configuration =>

  implicit val actorSystem: ActorSystem

  val http = Http(actorSystem)

  import VersionJsonProtocol._
  import spray.json._

  def upsertVersionStateApi(version: Version): Future[Unit] = {
    val stateVersion = StateVersion(
      candidate = version.candidate,
      version = version.version,
      vendor = version.vendor.getOrElse("DEFAULT"),
      url = version.url,
      platform = version.platform
    )
    val response =
      http.singleRequest(
        HttpRequest(
          uri = s"$stateApiBaseUrl/versions",
          method = HttpMethods.POST,
          entity = HttpEntity(ContentTypes.`application/json`, stateVersion.toJson.compactPrint)
        ).withHeaders(
          Authorization(
            BasicHttpCredentials(
              stateApiBasicAuthUsername,
              stateApiBasicAuthPassword
            )
          )
        )
      )
    response.flatMap {
      case HttpResponse(StatusCodes.NoContent, _, _, _) =>
        Future.successful(Unit)
      case HttpResponse(status, headers, entity, protocol) =>
        Future.failed(new RuntimeException(s"status: $status"))
      case _ => Future.failed(new RuntimeException)
    }
  }
}
