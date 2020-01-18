package io.sdkman.vendor.release.routes

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server.{Directive0, Directives}
import io.sdkman.vendor.release.Configuration

trait Authorisation {

  self: Directives with Configuration =>

  val ConsumerHeader = "Consumer"

  val AuthTokenHeader = "Service-Token"

  def authorised(candidate: String): Directive0 = authorize { rc =>
    val headers = rc.request.headers
    headers.exists(hasValidAuthTokenHeader) && headers.exists(
      implicit h => isValidConsumer(candidate)
    )
  }

  private def hasValidAuthTokenHeader(h: HttpHeader) =
    h.name() == AuthTokenHeader && h.value() == serviceToken

  private def isValidConsumer(c: String)(implicit h: HttpHeader) =
    h.name() == ConsumerHeader && h.value() == serviceAdminConsumer || h.value() == c

}
