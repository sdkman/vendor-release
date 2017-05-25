package io.sdkman.vendor.release

import akka.http.scaladsl.model.StatusCodes.Created
import akka.http.scaladsl.server.Directives
import io.sdkman.vendor.release.repos.{CandidatesRepo, Version, VersionsRepo}

import scala.concurrent.ExecutionContext.Implicits.global

trait ApiRoutes extends Directives with CandidatesRepo with VersionsRepo with HttpResponses with JsonSupport {

  val Universal = "UNIVERSAL"

  val apiRoute = path("release" / "multi") {
    post {
      entity(as[MultiPlatformReleaseRequest]) { req =>
        println(req)
        complete {
          Created
        }
      }
    }
  } ~ path("release" / "universal") {
    post {
      entity(as[UniversalPlatformReleaseRequest]) { req =>
        complete {
          val candidateFO = findCandidate(req.candidate)
          val versionFO = findVersion(req.candidate, req.version, Universal)
          for {
            candidateO <- candidateFO
            versionO <- versionFO
          } yield {
            candidateO.fold(badRequestResponseF(req)) { _ =>
              versionO.fold(saveVersion(Version(req.candidate, req.version, Universal, req.url))
                .map(_ => createdResponse(req.candidate, req.version, Universal)))(_ => conflictResponseF(req))
            }
          }
        }
      }
    }
  }
}