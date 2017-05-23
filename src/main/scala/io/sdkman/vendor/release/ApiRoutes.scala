package io.sdkman.vendor.release

import akka.http.scaladsl.model.StatusCodes.Created
import akka.http.scaladsl.server.Directives
import io.sdkman.vendor.release.repos.{Version, VersionsRepo}

import scala.concurrent.ExecutionContext.Implicits.global

trait ApiRoutes extends Directives with VersionsRepo with HttpResponses {

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
          findVersion(req.candidate, req.version, Universal).map { maybeVersion =>
            maybeVersion.map(v => conflictResponse(v)).getOrElse {
              val version = Version(req.candidate, req.version, Universal, req.url)
              saveVersion(version)
              createdResponse(version)
            }
          }
        }
      }
    }
  }
}