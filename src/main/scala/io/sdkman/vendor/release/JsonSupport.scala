package io.sdkman.vendor.release

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val multiPlatformFormat = jsonFormat1(MultiPlatformReleaseRequest)

  implicit val universalPlatformFormat = jsonFormat3(UniversalPlatformReleaseRequest)
}