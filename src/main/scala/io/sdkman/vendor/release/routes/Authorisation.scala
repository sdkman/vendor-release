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
