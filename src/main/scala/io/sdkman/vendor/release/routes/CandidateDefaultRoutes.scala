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
import io.sdkman.model.Version
import io.sdkman.repos.{CandidatesRepo, VersionsRepo}
import io.sdkman.vendor.release.{Configuration, HttpResponses}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait CandidateDefaultRoutes
    extends Directives
    with CandidatesRepo
    with VersionsRepo
    with MongoConnectivity
    with Configuration
    with MongoConfiguration
    with JsonSupport
    with HttpResponses
    with Authorisation
    with HttpStateApiClient {

  val candidateDefaultRoutes: Route = path("candidates" / "default") {
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
                    // Mongo default write first (authoritative), then best-effort State API tag
                    // propagation of the `lts` marker. The tag write never blocks or fails the
                    // 202 — Path 1's ordering (Mongo, then State) is deliberate.
                    for {
                      _ <- updateDefaultVersion(v.candidate, v.version)
                      _ <- propagateLtsTag(versions)
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

  /** Assert the `lts` tag on the State API for every Mongo platform row of the defaulted version,
    * reusing the `versions` Seq the route already fetched (no second query). Each row is wrapped
    * independently in [[bestEffortNonJava]], so java is skipped wholesale and one platform's
    * failure neither blocks the Mongo write nor suppresses its siblings. The writes run serially
    * so the first login populates the cached JWT and subsequent rows reuse it.
    *
    * Accepted cross-path platform-scope asymmetry: this path tags *all* platform rows of the
    * version, whereas Path 2 (`POST /versions`) tags only the single platform being posted. The
    * two coincide for the dominant UNIVERSAL non-java case — the single UNIVERSAL row.
    */
  private def propagateLtsTag(versions: Seq[Version]): Future[Unit] =
    versions.foldLeft(Future.unit) { (acc, v) =>
      acc.flatMap { _ =>
        bestEffortNonJava(v.candidate) {
          assignTagStateApi(
            candidate = v.candidate,
            version = v.version,
            distribution = v.vendor.flatMap(DistributionMapper.mapToStateDistribution),
            platform = PlatformMapper.mapToStatePlatform(v.platform),
            tag = HttpStateApiClient.LtsTag
          )
        }
      }
    }
}
