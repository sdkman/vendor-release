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
package io.sdkman.vendor.release

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpResponse, StatusCode}
import io.sdkman.vendor.release.routes.{ApiResponse, JsonSupport}
import spray.json._

import scala.concurrent.Future

trait HttpResponses extends JsonSupport {

  import ApiResponseJsonProtocol._

  def acceptedResponse(m: String) = HttpResponse(Accepted, entity = apiResponse(Accepted, m))

  def conflictResponse(c: String, v: String) = HttpResponse(Conflict, entity = apiResponse(Conflict, s"Duplicate: $c $v already exists"))

  def conflictResponseF(c: String, v: String) = Future.successful(conflictResponse(c, v))

  def createdResponse(c: String, v: String, p: String) = HttpResponse(Created, entity = apiResponse(Created, s"Released: $c $v for $p"))

  def badRequestResponse(m: String) = HttpResponse(BadRequest, entity = apiResponse(BadRequest, m))

  def badRequestResponseF(m: String) = Future.successful(badRequestResponse(m))

  private def apiResponse(status: StatusCode, message: String) = ApiResponse(status.intValue, message).toJson.compactPrint
}
