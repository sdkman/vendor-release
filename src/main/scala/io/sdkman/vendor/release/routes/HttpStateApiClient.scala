package io.sdkman.vendor.release.routes

import spray.json.DefaultJsonProtocol
import io.sdkman.vendor.release.Configuration
import io.sdkman.model.Version

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.scalalogging.LazyLogging

import scalaj.http.{Http, HttpResponse}

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

  import VersionJsonProtocol._
  import spray.json._

  def upsertVersionStateApi(version: Version): Future[Unit] = Future {
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

    val jsonBody = stateVersion.toJson.compactPrint

    val response: HttpResponse[String] = Http(s"$stateApiUrl/versions")
      .postData(jsonBody)
      .header("Content-Type", "application/json")
      .auth(stateApiBasicAuthUsername, stateApiBasicAuthPassword)
      .asString

    response.code match {
      case 204 =>
        logger.info(
          s"Upserted to $stateApiUrl/versions: ${version.candidate} ${version.version} ${version.platform} " +
            s"${version.vendor.getOrElse("")}"
        )
      case statusCode =>
        logger.error(
          s"Failed to upsert version to state API. Status: $statusCode, Body: ${response.body}"
        )
        throw new RuntimeException(
          s"State API request failed with status: $statusCode, body: ${response.body}"
        )
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
