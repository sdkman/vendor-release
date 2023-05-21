/**
  * Copyright 2020 Marco Vermeulen
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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ReleaseRoutes
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

  val Universal = "UNIVERSAL"

  val releaseRoutes = pathPrefix("release" / "version") {
    post {
      entity(as[PostReleaseRequest]) { req =>
        optionalHeaderValueByName("Vendor") { vendorHeader =>
          validate(req.candidate, req.version, req.platform, Some(req.url), req.checksums) {
            complete {
              onFinding(req.candidate, req.version, req.platform) {
                (candidateO, versionO, platform) =>
                  candidateO.fold(badRequestResponseF(s"Invalid candidate: ${req.candidate}")) {
                    c =>
                      //TODO: undo version-vendor concatenation once vendor domain is established
                      val vendor  = vendorHeader orElse req.vendor
                      val version = req.version + vendor.map(v => s"-$v").getOrElse("")
                      versionO.fold {
                        val v = Version(
                          candidate = c.candidate,
                          version = version,
                          platform = platform,
                          url = req.url,
                          vendor = vendor,
                          checksums = req.checksums
                        )
                        for {
                          _ <- saveVersion(v)
                          _ <- savePostgres(v)
                          _ <- if (req.default.exists(d => d)) for {
                            _ <- updateDefaultVersion(c.candidate, version)
                            _ <- updateDefaultVersionPostgres(c.candidate, version)
                          } yield ()
                          else Future.successful(Unit)
                        } yield createdResponse(c.candidate, version, platform)
                      }(v => conflictResponseF(c.candidate, v.version, platform))
                  }
              }
            }
          }
        }
      }
    } ~ patch {
      entity(as[PatchReleaseRequest]) { req =>
        validate(req.candidate, req.version, req.platform, req.url, req.checksums) {
          complete {
            onFinding(req.candidate, req.version, req.platform) {
              (candidateO, versionO, platform) =>
                val existing = for {
                  candidate       <- candidateO
                  existingVersion <- versionO
                } yield updateVersion(
                  existingVersion,
                  Version(
                    candidate.candidate,
                    existingVersion.version,
                    platform,
                    req.url getOrElse existingVersion.url,
                    req.vendor orElse existingVersion.vendor,
                    req.visible orElse existingVersion.visible,
                    req.checksums orElse existingVersion.checksums
                  )
                )
                existing.map(noContentResponseF()) getOrElse badRequestResponseF(
                  s"Does not exist: ${req.candidate} ${req.version} $platform"
                )
            }
          }
        }
      }
    } ~ delete {
      entity(as[DeleteReleaseRequest]) { req =>
        validate(req.candidate, req.version, Some(req.platform), None, None) {
          complete {
            findCandidate(req.candidate).flatMap {
              case Some(Candidate(_, _, _, Some(default), _, _)) if default == req.version =>
                conflictResponseF(req.candidate, req.version, req.platform)
              case _ =>
                deleteVersion(req.candidate, req.version, req.platform).map {
                  case result if result.getDeletedCount == 1 =>
                    okResponse(s"Deleted: ${req.candidate} ${req.version} ${req.platform}")
                  case _ =>
                    notFoundResponse(s"Not found: ${req.candidate} ${req.version} ${req.platform}")
                }
            }
          }
        }
      }
    }
  }

  private def validate(
      candidate: String,
      version: String,
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

  private def savePostgres(v: Version): Future[Unit] = Future.successful(Unit)
  private def updateDefaultVersionPostgres(candidate: String, version: String): Future[Unit] =
    Future.successful(Unit)
}
