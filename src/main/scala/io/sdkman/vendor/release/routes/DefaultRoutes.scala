package io.sdkman.vendor.release.routes

import akka.http.scaladsl.server.Directives
import io.sdkman.db.{MongoConfiguration, MongoConnectivity}
import io.sdkman.repos.{CandidatesRepo, VersionsRepo}
import io.sdkman.vendor.release.{Configuration, HttpResponses}

import scala.concurrent.ExecutionContext.Implicits.global

trait DefaultRoutes extends Directives
  with CandidatesRepo
  with VersionsRepo
  with MongoConnectivity
  with Configuration
  with MongoConfiguration
  with JsonSupport
  with HttpResponses
  with Authorisation {

  val defaultRoutes = path("default" / "version") {
    put {
      entity(as[VersionDefaultRequest]) { req =>
        authorised(req.candidate) {
          val candidateFO = findCandidate(req.candidate)
          val versionsF = findAllVersionsByCandidateVersion(req.candidate, req.version)
          complete {
            for {
              candidateO <- candidateFO
              versions <- versionsF
            } yield {
              candidateO.fold(badRequestResponseF(s"Invalid candidate: ${req.candidate}")) { _ =>
                versions.headOption.map { v =>
                  updateDefaultVersion(req.candidate, req.version)
                    .map(_ => acceptedResponse(s"Defaulted: ${req.candidate} ${req.version}"))
                }.getOrElse(badRequestResponseF(s"Invalid candidate version: ${req.candidate} ${req.version}"))
              }
            }
          }
        }
      }
    }
  }
}