package io.sdkman.vendor.release.routes

import akka.http.scaladsl.server.{Directive0, Directives}
import io.sdkman.UrlValidation

trait Validation {

  self: Directives with UrlValidation with JsonSupport =>

  import ApiResponseJsonProtocol._
  import spray.json._

  val SupportedPlatforms = Seq("LINUX_64", "LINUX_32", "LINUX_ARM64", "LINUX_ARM32", "MAC_OSX", "WINDOWS_64", "UNIVERSAL")

  def validatePlatform(platform: String): Directive0 =
    validate(
      SupportedPlatforms.contains(platform),
      ApiResponse(400, s"Invalid platform: $platform").toJson.compactPrint
    )

  def validateUrl(url: String): Directive0 =
    validate(
      resourceAvailable(url),
      ApiResponse(400, s"URL cannot be resolved: $url").toJson.compactPrint
    )

  def validateVersion(version: String): Directive0 =
    validate(
      15 >= version.length,
      ApiResponse(400, s"Version length exceeds 15 chars: $version").toJson.compactPrint
    )
}
