package io.sdkman.vendor.release

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.{Conflict, Created}
import io.sdkman.vendor.release.repos.Version

trait HttpResponses {
  def createdResponse(v: Version) = HttpResponse(Created, entity = s"Released: ${v.candidate} ${v.version} for ${v.platform}")

  def conflictResponse(v: Version) = HttpResponse(Conflict, entity = s"Duplicate: ${v.candidate} ${v.version} already exists")
}
