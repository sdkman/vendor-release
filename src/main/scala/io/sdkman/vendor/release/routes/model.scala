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
