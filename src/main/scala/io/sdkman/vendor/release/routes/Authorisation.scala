/**
 * Copyright 2023 SDKMAN!
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

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server.{Directive0, Directives}
import io.sdkman.vendor.release.Configuration

trait Authorisation {

  self: Directives with Configuration =>

  private val CandidatesHeader = "Candidates"

  private val AuthTokenHeader = "Service-Token"

  def authorised(candidate: String): Directive0 = authorize { rc =>
    val headers = rc.request.headers
    headers.exists(hasValidAuthTokenHeader) &&
    headers.exists(isValidConsumer(candidate))
  }

  private def hasValidAuthTokenHeader(h: HttpHeader) =
    h.name() == AuthTokenHeader && h.value() == serviceToken

  private def isValidConsumer(c: String)(h: HttpHeader) =
    h.name() == CandidatesHeader &&
      h.value().split('|').contains(c) ||
      h.value() == serviceAdminConsumer

}
