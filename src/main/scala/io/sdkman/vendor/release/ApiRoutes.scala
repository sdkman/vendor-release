package io.sdkman.vendor.release

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.{Conflict, Created}
import akka.http.scaladsl.server.Directives
import io.sdkman.vendor.release.repos.{Version, VersionsRepo}
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global

case class UniversalPlatformReleaseRequest(candidate: String, version: String, url: String)

case class MultiPlatformReleaseRequest(payload: List[String])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val multiPlatformFormat = jsonFormat1(MultiPlatformReleaseRequest)
  implicit val universalPlatformFormat = jsonFormat3(UniversalPlatformReleaseRequest)
}

trait ApiRoutes extends Directives with JsonSupport with VersionsRepo {

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
          findVersion(req.candidate, req.version, Universal).map { vO =>
            vO.map(v => HttpResponse(Conflict, entity = s"Duplicate: ${v.candidate} ${v.version} already exists")).getOrElse {
              saveVersion(Version(req.candidate, req.version, Universal, req.url))
              HttpResponse(Created, entity = s"Released: ${req.candidate} ${req.version} for $Universal")
            }
          }
        }
      }
    }
  }
}