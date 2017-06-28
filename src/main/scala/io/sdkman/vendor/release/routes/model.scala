package io.sdkman.vendor.release.routes

case class VersionDefaultRequest(candidate: String, version: String)

case class VersionReleaseRequest(candidate: String, version: String, url: String, platform: Option[String])

case class ApiResponse(status: Int, message: String)


