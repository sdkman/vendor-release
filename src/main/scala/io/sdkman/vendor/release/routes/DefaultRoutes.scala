package io.sdkman.vendor.release.routes

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives
import io.sdkman.vendor.release.repos.{CandidatesRepo, VersionsRepo}

import scala.concurrent.ExecutionContext.Implicits.global

trait DefaultRoutes extends Directives with CandidatesRepo with VersionsRepo with JsonSupport {
  val defaultRoutes = path("default" / "version") {
    put {
      entity(as[VersionDefaultRequest]) { req =>
        complete {
          updateDefaultVersion(req.candidate, req.default).map { _ =>
            HttpResponse(StatusCodes.Accepted, entity = s"Defaulted: ${req.candidate} ${req.default}")
          }
        }
      }
    }
  }
}

case class VersionDefaultRequest(candidate: String, default: String)
