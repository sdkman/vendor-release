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

import akka.http.scaladsl.server.{Directives, Route}
import io.sdkman.db.{MongoConfiguration, MongoConnectivity}
import io.sdkman.repos.{CandidatesRepo, VersionsRepo}
import io.sdkman.vendor.release.repos.PgCandidateRepo
import io.sdkman.vendor.release.{Configuration, HttpResponses, PostgresConnectivity}

import scala.concurrent.ExecutionContext.Implicits.global

trait VersionDefaultRoutes
    extends Directives
    with CandidatesRepo
    with VersionsRepo
    with MongoConnectivity
    with Configuration
    with MongoConfiguration
    with PostgresConnectivity
    with PgCandidateRepo
    with JsonSupport
    with HttpResponses
    with Authorisation {

  val versionDefaultRoutes: Route = path("default") {
    put {
      entity(as[DefaultVersionRequest]) { req =>
        authorised(req.candidate) {
          val candidateFO = findCandidate(req.candidate)
          val versionsF   = findAllVersionsByCandidateVersion(req.candidate, req.version)
          complete {
            for {
              candidateO <- candidateFO
              versions   <- versionsF
            } yield {
              candidateO.fold(badRequestResponseF(s"Invalid candidate: ${req.candidate}")) { _ =>
                versions.headOption
                  .map { v =>
                    for {
                      _ <- updateDefaultVersion(v.candidate, v.version)
                      _ <- updateDefaultVersionPostgres(v.candidate, v.version)
                    } yield acceptedResponse(s"Defaulted: ${v.candidate} ${v.version}")
                  }
                  .getOrElse(
                    badRequestResponseF(
                      s"Invalid candidate version: ${req.candidate} ${req.version}"
                    )
                  )
              }
            }
          }
        }
      }
    }
  }
}
