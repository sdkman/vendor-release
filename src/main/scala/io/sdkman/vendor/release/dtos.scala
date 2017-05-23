package io.sdkman.vendor.release

case class UniversalPlatformReleaseRequest(candidate: String, version: String, url: String)

case class MultiPlatformReleaseRequest(payload: List[String])
