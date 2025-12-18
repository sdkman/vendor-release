package io.sdkman.vendor.release.routes

import com.typesafe.scalalogging.LazyLogging

object DistributionMapper extends LazyLogging {
  private val distributionMapping = Map(
    "albba"   -> "DRAGONWELL",
    "amzn"    -> "CORRETTO",
    "bisheng" -> "BISHENG",
    "graalce" -> "GRAALCE",
    "graalvm" -> "GRAALVM",
    "jbr"     -> "JETBRAINS",
    "kona"    -> "KONA",
    "librca"  -> "LIBERICA",
    "mandrel" -> "MANDREL",
    "ms"      -> "MICROSOFT",
    "nik"     -> "LIBERICA_NIK",
    "open"    -> "OPENJDK",
    "oracle"  -> "ORACLE",
    "sapmchn" -> "SAP_MACHINE",
    "sem"     -> "SEMERU",
    "tem"     -> "TEMURIN",
    "zulu"    -> "ZULU"
  )

  def mapToStateDistribution(vendor: String): Option[String] =
    distributionMapping.get(vendor) match {
      case Some(stateDistribution) =>
        logger.debug(s"Mapped vendor $vendor to $stateDistribution")
        Some(stateDistribution)
      case None =>
        logger.warn(s"Unknown vendor $vendor, defaulting to None")
        None
    }
}
