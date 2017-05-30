package io.sdkman.vendor.release.routes

import akka.http.scaladsl.server.Directives
import io.sdkman.vendor.release.HttpResponses
import io.sdkman.vendor.release.repos.{CandidatesRepo, VersionsRepo}

import scala.concurrent.ExecutionContext.Implicits.global

trait DefaultRoutes extends Directives with CandidatesRepo with VersionsRepo with JsonSupport with HttpResponses {
  val defaultRoutes = path("default" / "version") {
    put {
      entity(as[VersionDefaultRequest]) { req =>
        val candidateFO = findCandidate(req.candidate)
        val versionsF = findAllVersions(req.candidate, req.default)
        complete {
          for {
            candidateO <- candidateFO
            versions <- versionsF
          } yield {
            candidateO.fold(badRequestResponseF(s"Invalid candidate: ${req.candidate}")) { _ =>
              versions.headOption.map { v =>
                updateDefaultVersion(req.candidate, req.default)
                  .map(_ => acceptedResponse(s"Defaulted: ${req.candidate} ${req.default}"))
              }.getOrElse(badRequestResponseF(s"Invalid candidate version: ${req.candidate} ${req.default}"))
            }
          }
        }
      }
    }
  }
}


case class VersionDefaultRequest(candidate: String, default: String)
