package io.sdkman.vendor.release.routes

import akka.http.scaladsl.server.{Directives, Route}
import io.sdkman.db.{MongoConfiguration, MongoConnectivity}
import io.sdkman.repos.{CandidatesRepo, VersionsRepo}
import io.sdkman.vendor.release.repos.PgCandidateRepo
import io.sdkman.vendor.release.{Configuration, HttpResponses, PostgresConnectivity}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DefaultRoutes
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

  val defaultRoutes: Route = path("default" / "version") {
    put {
      entity(as[VersionDefaultRequest]) { req =>
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
