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
    "LINUX_ARM32",
    "MAC_OSX",
    "MAC_ARM64",
    "WINDOWS_64",
    "UNIVERSAL"
  )

  val Algorithms = Map(
    (MD5.id, "^[a-f0-9]{32}$"),
    (SHA1.id, "^[a-f0-9]{40}$"),
    (SHA224.id, "^[a-f0-9]{56}$"),
    (SHA256.id, "^[a-f0-9]{64}$"),
    (SHA384.id, "^[a-f0-9]{96}$"),
    (SHA512.id, "^[a-f0-9]{128}$")
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

  def validateChecksumAlgorithms(checksums: Option[Map[String,String]]): Directive0 =
    checksums
      .map { checksum =>

        val invalidAlgorithms = checksum.keys.filter { key =>
            !Algorithms.contains(key)
        }

        validate(
          check = invalidAlgorithms.isEmpty,
          ApiResponse(400, s"Invalid algorithm(s): ${invalidAlgorithms.mkString(", ")}").toJson.compactPrint)


      } getOrElse pass

  def validateChecksums(checksums: Option[Map[String, String]]): Directive0 =
    checksums
      .map { checksum =>

        val invalidChecksumAlgorithms = checksum.filter { entry =>
          val regex = Algorithms.get(entry._1)
          entry._2 == null || regex.isEmpty || !entry._2.matches(regex.get)
        }.keys
          .map { algorithm =>
            algorithm match {
              case MD5.id => (algorithm, MD5.priority)
              case SHA1.id => (algorithm, SHA1.priority)
              case SHA224.id => (algorithm, SHA224.priority)
              case SHA256.id => (algorithm, SHA256.priority)
              case SHA384.id => (algorithm, SHA384.priority)
              case SHA512.id => (algorithm, SHA512.priority)
              case _ => (algorithm,  Integer.MAX_VALUE)
            }
          }
          .toSeq
          .sortBy(kv => (kv._2.asInstanceOf[Integer], kv._1))
          .map(_._1)

        validate(invalidChecksumAlgorithms.isEmpty,
          ApiResponse(400, s"Invalid checksum for algorithm(s): ${invalidChecksumAlgorithms.mkString(", ")}").toJson.compactPrint)

      } getOrElse pass
}
