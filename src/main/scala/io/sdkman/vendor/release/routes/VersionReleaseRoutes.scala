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
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.model.StatusCodes
import akka.util.ByteString
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.headers.Authorization

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
    with Authorisation
    with HttpStateApiClient {

  private val Universal = "UNIVERSAL"

  val versionReleaseRoutes: Route = path("versions") {
    post {
      entity(as[PostVersionReleaseRequest]) { req =>
        optionalHeaderValueByName("Vendor") { vendorHeader =>
          validate(req.candidate, req.version, req.platform, Some(req.url), req.checksums) {
            complete {
              onFinding(req.candidate, req.version, req.platform) { (candidateO, _, platform) =>
                candidateO.fold(badRequestResponseF(s"Invalid candidate: ${req.candidate}")) { c =>
                  val vendor = vendorHeader orElse req.vendor
                  val v = Version(
                    candidate = c.candidate,
                    version = req.version,
                    platform = platform,
                    url = req.url,
                    vendor = vendor,
                    checksums = req.checksums
                  )
                  val mongoVersionString = req.version + vendor.map(v => s"-$v").getOrElse("")
                  for {
                    _ <- upsertVersionMongodb(v.copy(version = mongoVersionString))
                    _ <- upsertVersionStateApi(v).recoverWith {
                      case ex: Exception =>
                        logger.error(
                          s"Failed to upsert version to state API: ${ex.getMessage}",
                          ex
                        )
                        Future.unit
                    }
                    _ <- if (req.default.exists(d => d)) for {
                      _ <- updateDefaultVersion(c.candidate, mongoVersionString)
                    } yield ()
                    else Future.successful(Unit)
                  } yield createdResponse(c.candidate, mongoVersionString, platform)
                }
              }
            }
          }
        }
      }
    } ~ delete {
      entity(as[DeleteVersionReleaseRequest]) { req =>
        validate(req.candidate, req.version, Some(req.platform), None, None) {
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
      version: String,
      platform: Option[String],
      url: Option[String],
      checksums: Option[Map[String, String]]
  )(
      route: StandardRoute
  ): Route = {
    authorised(candidate) {
      validateVersionFormat(version) {
        validatePlatform(platform) {
          validateUrl(url) {
            validateChecksumAlgorithms(checksums) {
              validateChecksums(checksums)(route)
            }
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
