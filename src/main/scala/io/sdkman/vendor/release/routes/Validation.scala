package io.sdkman.vendor.release.routes

import akka.http.scaladsl.server.{Directive0, Directives}
import io.sdkman.UrlValidation

trait Validation {

  self: Directives with UrlValidation with JsonSupport =>

  import ApiResponseJsonProtocol._
  import spray.json._

  val SupportedPlatforms = Seq(
    "LINUX_64",
    "LINUX_32",
    "LINUX_ARM64",
    "LINUX_ARM32",
    "MAC_OSX",
    "MAC_ARM64",
    "WINDOWS_64",
    "UNIVERSAL"
  )

  def validatePlatform(platform: Option[String]): Directive0 =
    platform
      .map(
        p =>
          validate(
            SupportedPlatforms.contains(p),
            ApiResponse(400, s"Invalid platform: $p").toJson.compactPrint
          )
      ) getOrElse pass

  def validateUrl(url: Option[String]): Directive0 =
    url
      .map { u =>
        validate(
          resourceAvailable(u),
          ApiResponse(400, s"URL cannot be resolved: $u").toJson.compactPrint
        )
      } getOrElse pass

  def validateVersion(version: String): Directive0 =
    validate(
      17 >= version.length,
      ApiResponse(400, s"Version length exceeds 17 chars: $version").toJson.compactPrint
    )
}
