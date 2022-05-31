package io.sdkman.vendor.release.routes

case class VersionDefaultRequest(candidate: String, version: String)

case class PostReleaseRequest(
    candidate: String,
    version: String,
    url: String,
    platform: Option[String],
    vendor: Option[String],
    checksums: Option[Map[String, String]] = None,
    default: Option[Boolean] = Some(false)
)

case class PatchReleaseRequest(
    candidate: String,
    version: String,
    platform: Option[String],
    url: Option[String],
    vendor: Option[String],
    visible: Option[Boolean],
    checksums: Option[Map[String, String]] = None
)

case class DeleteReleaseRequest(
    candidate: String,
    version: String,
    platform: String
)

case class ApiResponse(status: Int, message: String)
