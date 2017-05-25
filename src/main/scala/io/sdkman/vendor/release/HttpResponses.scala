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
