package io.sdkman.vendor.release.routes

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server.{Directive0, Directives}

trait Authorisation {

  self: Directives =>

  val ConsumerHeader = "Consumer"

  val AdminConsumer = "default_admin"

  val AuthTokenHeader = "Auth-Token"

  val TokenValue = "default_token"

  def authorised(candidate: String): Directive0 = authorize { rc =>
    val headers = rc.request.headers
    headers.exists(hasValidAuthTokenHeader) && headers.exists(implicit h => isValidConsumer(candidate))
  }

  private def hasValidAuthTokenHeader(h: HttpHeader) = h.name() == AuthTokenHeader && h.value() == TokenValue

  private def isValidConsumer(c: String)(implicit h: HttpHeader) =
    h.name() == ConsumerHeader && h.value() == AdminConsumer || h.value() == c

}
