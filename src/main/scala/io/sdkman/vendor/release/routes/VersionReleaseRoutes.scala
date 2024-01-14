/**
  * Copyright 2023 SDKMAN!
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package io.sdkman.vendor.release.routes

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.{Directives, Route, StandardRoute}
import com.typesafe.scalalogging.LazyLogging
import io.sdkman.UrlValidation
import io.sdkman.db.{MongoConfiguration, MongoConnectivity}
import io.sdkman.model.{Candidate, Version}
import io.sdkman.repos.{CandidatesRepo, VersionsRepo}
import io.sdkman.vendor.release.{Configuration, HttpResponses}
import org.mongodb.scala.Completed

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait VersionReleaseRoutes
    extends Directives
    with CandidatesRepo
    with VersionsRepo
    with MongoConnectivity
    with MongoConfiguration
    with Configuration
    with HttpResponses
    with JsonSupport
    with Validation
    with UrlValidation
    with LazyLogging
    with Authorisation {

  private val Universal = "UNIVERSAL"

  val versionReleaseRoutes: Route = path("versions") {
    post {
      entity(as[PostVersionReleaseRequest]) { req =>
        optionalHeaderValueByName("Vendor") { vendorHeader =>
          validate(req.candidate, req.platform, Some(req.url), req.checksums) {
            complete {
              onFinding(req.candidate, req.version, req.platform) { (candidateO, _, platform) =>
                candidateO.fold(badRequestResponseF(s"Invalid candidate: ${req.candidate}")) { c =>
                  //TODO: undo version-vendor concatenation once vendor domain is established
                  val vendor  = vendorHeader orElse req.vendor
                  val version = req.version + vendor.map(v => s"-$v").getOrElse("")
                  val v = Version(
                    candidate = c.candidate,
                    version = version,
                    platform = platform,
                    url = req.url,
                    vendor = vendor,
                    checksums = req.checksums
                  )
                  for {
                    _ <- upsertVersionMongodb(v)
                    _ <- if (req.default.exists(d => d)) for {
                      _ <- updateDefaultVersion(c.candidate, version)
                    } yield ()
                    else Future.successful(Unit)
                  } yield createdResponse(c.candidate, version, platform)
                }
              }
            }
          }
        }
      }
    } ~ patch {
      entity(as[PatchVersionReleaseRequest]) { req =>
        validate(req.candidate, req.platform, req.url, req.checksums) {
          complete {
            onFinding(req.candidate, req.version, req.platform) {
              (candidateO, versionO, platform) =>
                val existing = for {
                  existingCandidate <- candidateO
                  oldVersion        <- versionO
                  newVersion = Version(
                    existingCandidate.candidate,
                    oldVersion.version,
                    platform,
                    req.url getOrElse oldVersion.url,
                    req.vendor orElse oldVersion.vendor,
                    req.visible orElse oldVersion.visible,
                    req.checksums orElse oldVersion.checksums
                  )
                } yield updateVersion(oldVersion, newVersion)
                  .map(res => res)
                existing.map(noContentResponseF()) getOrElse badRequestResponseF(
                  s"Does not exist: ${req.candidate} ${req.version} $platform"
                )
            }
          }
        }
      }
    } ~ delete {
      entity(as[DeleteVersionReleaseRequest]) { req =>
        validate(req.candidate, Some(req.platform), None, None) {
          complete {
            findCandidate(req.candidate).flatMap {
              case Some(Candidate(_, _, _, Some(default), _, _)) if default == req.version =>
                conflictResponseF(req.candidate, req.version, req.platform)
              case _ =>
                deleteVersion(req.candidate, req.version, req.platform)
                  .map(delMdb => delMdb)
                  .map {
                    case delMdb if delMdb.getDeletedCount == 1 =>
                      okResponse(s"Deleted: ${req.candidate} ${req.version} ${req.platform}")
                    case _ =>
                      notFoundResponse(
                        s"Not found: ${req.candidate} ${req.version} ${req.platform}"
                      )
                  }
            }
          }
        }
      }
    }
  }

  private def validate(
      candidate: String,
      platform: Option[String],
      url: Option[String],
      checksums: Option[Map[String, String]]
  )(
      route: StandardRoute
  ): Route = {
    authorised(candidate) {
      validatePlatform(platform) {
        validateUrl(url) {
          validateChecksumAlgorithms(checksums) {
            validateChecksums(checksums)(route)
          }
        }
      }
    }
  }

  private def onFinding(candidate: String, version: String, platform: Option[String])(
      f: (Option[Candidate], Option[Version], String) => Future[HttpResponse]
  ) = {
    val resolvedPlatform = platform.getOrElse(Universal)
    val candidateFO      = findCandidate(candidate)
    val versionFO        = findVersion(candidate, version, resolvedPlatform)
    for {
      candidateO <- candidateFO
      versionO   <- versionFO
      response   <- f(candidateO, versionO, resolvedPlatform)
    } yield response
  }

  private def upsertVersionMongodb(version: Version): Future[Completed] =
    for {
      result <- updateVersion(version, version)
      updated = result.getModifiedCount > 0
      insert <- if (!updated) saveVersion(version) else Future.successful(Completed())
    } yield insert
}
