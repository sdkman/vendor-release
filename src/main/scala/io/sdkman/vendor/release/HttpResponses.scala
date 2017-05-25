/**
  * Copyright 2017 Marco Vermeulen
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

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.{BadRequest, Conflict, Created}

import scala.concurrent.Future

trait HttpResponses {
  def conflictResponse(c: String, v: String) = HttpResponse(Conflict, entity = s"Duplicate: $c $v already exists")

  def conflictResponseF(req: UniversalPlatformReleaseRequest) = Future.successful(conflictResponse(req.candidate, req.version))

  def createdResponse(c: String, v: String, p: String) = HttpResponse(Created, entity = s"Released: $c $v for $p")

  def badRequestResponse(c: String) = HttpResponse(BadRequest, entity = s"Invalid candidate: $c")

  def badRequestResponseF(req: UniversalPlatformReleaseRequest) = Future.successful(badRequestResponse(req.candidate))
}
