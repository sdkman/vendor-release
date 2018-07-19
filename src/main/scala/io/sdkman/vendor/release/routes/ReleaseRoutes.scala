/**
  * Copyright 2018 Marco Vermeulen
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

import akka.http.scaladsl.server.Directives
import com.typesafe.scalalogging.LazyLogging
import io.sdkman.UrlValidation
import io.sdkman.db.{MongoConfiguration, MongoConnectivity}
import io.sdkman.repos.{CandidatesRepo, Version, VersionsRepo}
import io.sdkman.vendor.release.{Configuration, HttpResponses}

import scala.concurrent.ExecutionContext.Implicits.global

trait ReleaseRoutes extends Directives
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

  val releaseRoutes = path("release" / "version") {
    post {
      entity(as[VersionReleaseRequest]) { req =>
        authorised(req.candidate) {
          val platform = req.platform.getOrElse(Universal)
          validatePlatform(platform) {
            validateVersion(req.version) {
              validateUrl(req.url) {
                complete {
                  val candidateFO = findCandidate(req.candidate)
                  val versionFO = findVersion(req.candidate, req.version, platform)
                  for {
                    candidateO <- candidateFO
                    versionO <- versionFO
                  } yield {
                    candidateO.fold(badRequestResponseF(s"Invalid candidate: ${req.candidate}")) { _ =>
                      versionO.fold(saveVersion(Version(req.candidate, req.version, platform, req.url))
                        .map(_ => createdResponse(req.candidate, req.version, platform))) { _ =>
                        conflictResponseF(req.candidate, req.version)
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}