/**
  * Copyright 2017 Marco Vermeulen
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
import io.sdkman.vendor.release.{Configuration, HttpResponses}
import io.sdkman.vendor.release.repos.{CandidatesRepo, Version, VersionsRepo}

import scala.concurrent.ExecutionContext.Implicits.global

trait ReleaseRoutes extends Directives
  with Configuration
  with CandidatesRepo
  with VersionsRepo
  with HttpResponses
  with JsonSupport
  with PlatformValidation
  with Authorisation {

  val Universal = "UNIVERSAL"

  val releaseRoutes = path("release" / "version") {
    post {
      entity(as[VersionReleaseRequest]) { req =>
        authorised(req.candidate) {
          val platform = req.platform.getOrElse(Universal)
          validatePlatform(platform) {
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