package io.sdkman.vendor.release.routes

import akka.http.scaladsl.server.{Directive0, Directives}

trait PlatformValidation {

  self: Directives =>

  val SupportedPlatforms = Seq("LINUX_64", "LINUX_32", "MAC_OSX", "WINDOWS_64", "UNIVERSAL")

  def validatePlatform(platform: String): Directive0 =
    validate(SupportedPlatforms.contains(platform), s"Invalid platform: $platform")

}