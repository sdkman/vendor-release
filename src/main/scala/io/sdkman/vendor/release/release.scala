package io.sdkman.vendor

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.sdkman.vendor.release.{MultiPlatformReleaseRequest, UniversalPlatformReleaseRequest}
import spray.json.DefaultJsonProtocol

package object release extends io.sdkman.vendor.JsonSupport

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val multiPlatformFormat = jsonFormat1(MultiPlatformReleaseRequest)

  implicit val universalPlatformFormat = jsonFormat3(UniversalPlatformReleaseRequest)
}