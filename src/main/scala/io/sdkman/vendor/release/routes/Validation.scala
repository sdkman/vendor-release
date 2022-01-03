package io.sdkman.vendor.release.routes

import akka.http.scaladsl.server.{Directive0, Directives}
import io.sdkman.UrlValidation
import io.sdkman.model._

trait Validation {

  self: Directives with UrlValidation with JsonSupport =>

  import ApiResponseJsonProtocol._
  import spray.json._

  val SupportedPlatforms = Seq(
    "LINUX_64",
    "LINUX_32",
    "LINUX_ARM64",
    "LINUX_ARM32SF",
    "LINUX_ARM32HF",
    "MAC_OSX",
    "MAC_ARM64",
    "WINDOWS_64",
    "UNIVERSAL"
  )

  val AlgorithmRegex = Map(
    MD5.id    -> "^[a-f0-9]{32}$",
    SHA1.id   -> "^[a-f0-9]{40}$",
    SHA224.id -> "^[a-f0-9]{56}$",
    SHA256.id -> "^[a-f0-9]{64}$",
    SHA384.id -> "^[a-f0-9]{96}$",
    SHA512.id -> "^[a-f0-9]{128}$"
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

  def validateChecksumAlgorithms(checksums: Option[Map[String, String]]): Directive0 =
    checksums.map { csMap =>
      val invalidAlgorithms = csMap.keys.filter(!AlgorithmRegex.contains(_))

      validate(
        check = invalidAlgorithms.isEmpty,
        ApiResponse(400, s"Invalid algorithm(s): ${invalidAlgorithms.mkString(",")}").toJson.compactPrint
      )

    } getOrElse pass

  def validateChecksums(checksums: Option[Map[String, String]]): Directive0 =
    checksums
      .map { csMap =>
        val invalidChecksumAlgorithms = csMap
          .filter {
            case (algorithm: String, hash: String) =>
              hash == null ||
                hash.isBlank ||
                !hash.matches(AlgorithmRegex.getOrElse(algorithm, "NA"))
          }
          .keys
          .toList
          .sorted

        validate(
          invalidChecksumAlgorithms.isEmpty,
          ApiResponse(
            400,
            s"Invalid checksum for algorithm(s): ${invalidChecksumAlgorithms.mkString(",")}"
          ).toJson.compactPrint
        )

      } getOrElse pass
}
