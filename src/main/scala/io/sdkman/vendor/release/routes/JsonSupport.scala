/**
  * Copyright 2020 Marco Vermeulen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package io.sdkman.vendor.release.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{
  DefaultJsonProtocol,
  DeserializationException,
  JsNumber,
  JsObject,
  JsString,
  JsValue,
  RootJsonFormat
}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val postReleaseFormat = jsonFormat7(PostReleaseRequest)

  implicit val patchReleaseFormat = jsonFormat7(PatchReleaseRequest)

  implicit val deleteReleaseFormat = jsonFormat3(DeleteReleaseRequest)

  implicit val versionDefaultFormat = jsonFormat2(VersionDefaultRequest)

  object ApiResponseJsonProtocol extends DefaultJsonProtocol {
    implicit object ApiReponseJsonFormat extends RootJsonFormat[ApiResponse] {
      def write(ar: ApiResponse) =
        JsObject("status" -> JsNumber(ar.status), "message" -> JsString(ar.message))
      def read(value: JsValue) = {
        value.asJsObject.getFields("status", "message") match {
          case Seq(JsNumber(status), JsString(message)) =>
            ApiResponse(status.intValue, message)
          case _ =>
            throw DeserializationException("ApiResponse expected")
        }
      }
    }
  }

}
