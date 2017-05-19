package io.sdkman.vendor.release

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.Created
import akka.http.scaladsl.server.Directives
import io.sdkman.vendor.release.repos.{Version, VersionsRepo}
import org.mongodb.scala.Completed
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
      entity(as[UniversalPlatformReleaseRequest]) { request =>
        complete {
          saveVersion(Version(request.candidate, request.version, Universal, request.url)).toFuture.map { completeions =>
            completeions.headOption.map(_ =>
                HttpResponse(Created, entity = releaseMessage(request.candidate, request.version, Universal)))
          }
        }
      }
    }
  }

  def releaseMessage(candidate: String, version: String, platform: String) = s"Released $candidate $version for $platform"
}