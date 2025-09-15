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
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.duration._

case class StateVersion(
    candidate: String,
    version: String,
    vendor: Option[String],
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

trait HttpStateApiClient extends LazyLogging {
  self: Configuration =>

  implicit val actorSystem: ActorSystem

  lazy val http = Http(actorSystem)

  import VersionJsonProtocol._
  import spray.json._

  def upsertVersionStateApi(version: Version): Future[Unit] = {
    logger.info(
      s"Upserting version to state API: ${version.candidate} ${version.version} ${version.platform}"
    )

    val statePlatform                  = PlatformMapper.mapToStatePlatform(version.platform)
    val (md5sum, sha256sum, sha512sum) = extractChecksums(version.checksums)

    val stateVersion = StateVersion(
      candidate = version.candidate,
      version = version.version,
      vendor = version.vendor,
      url = version.url,
      platform = statePlatform,
      visible = version.visible.getOrElse(true),
      md5sum = md5sum,
      sha256sum = sha256sum,
      sha512sum = sha512sum
    )

    logger.debug(s"State API payload: ${stateVersion.toJson.prettyPrint}")

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
        logger.info(
          s"Successfully upserted version to state API: ${version.candidate} ${version.version}"
        )
        Future.successful(Unit)
      case HttpResponse(status, _, entity, _) =>
        entity.toStrict(5.seconds).map { strictEntity =>
          val errorBody = strictEntity.data.utf8String
          logger
            .error(s"Failed to upsert version to state API. Status: $status, Body: $errorBody")
          throw new RuntimeException(
            s"State API request failed with status: $status, body: $errorBody"
          )
        }
      case _ =>
        logger.error("Unexpected response from state API")
        Future.failed(new RuntimeException("Unexpected response from state API"))
    }
  }

  private def extractChecksums(
      checksums: Option[Map[String, String]]
  ): (Option[String], Option[String], Option[String]) = {
    checksums match {
      case Some(checksumMap) =>
        val md5 = checksumMap.get("MD5").orElse(checksumMap.get("md5"))
        val sha256 = checksumMap
          .get("SHA-256")
          .orElse(checksumMap.get("sha256"))
          .orElse(checksumMap.get("SHA256"))
        val sha512 = checksumMap
          .get("SHA-512")
          .orElse(checksumMap.get("sha512"))
          .orElse(checksumMap.get("SHA512"))
        (md5, sha256, sha512)
      case None => (None, None, None)
    }
  }
}
