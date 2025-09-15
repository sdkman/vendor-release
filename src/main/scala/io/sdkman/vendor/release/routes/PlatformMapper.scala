package io.sdkman.vendor.release.routes

import com.typesafe.scalalogging.LazyLogging

object PlatformMapper extends LazyLogging {

  val platformMapping: Map[String, String] = Map(
    "MAC_OSX"       -> "MAC_X64",
    "MAC_ARM64"     -> "MAC_ARM64",
    "LINUX_64"      -> "LINUX_X64",
    "LINUX_32"      -> "LINUX_X32",
    "LINUX_ARM32HF" -> "LINUX_ARM32HF",
    "LINUX_ARM32SF" -> "LINUX_ARM32SF",
    "LINUX_ARM64"   -> "LINUX_ARM64",
    "WINDOWS_64"    -> "WINDOWS_X64",
    "UNIVERSAL"     -> "UNIVERSAL"
  )

  def mapToStatePlatform(vendorPlatform: String): String = {
    platformMapping.get(vendorPlatform) match {
      case Some(statePlatform) =>
        logger.debug(s"Mapped platform $vendorPlatform to $statePlatform")
        statePlatform
      case None =>
        logger.warn(s"Unknown platform $vendorPlatform, defaulting to UNIVERSAL")
        "UNIVERSAL"
    }
  }
}
