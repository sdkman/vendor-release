/**
  * Copyright 2017 Marco Vermeulen
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
package io.sdkman.vendor.release.repos

import io.sdkman.vendor.release.{Configuration, MongoConnectivity}
import org.mongodb.scala.Completed
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters.{and, equal}

import scala.concurrent.Future

trait VersionsRepo extends MongoConnectivity {

  self: Configuration =>

  def saveVersion(v: Version): Future[Completed] =
    versionsCollection.insertOne(
      Document(
        "candidate" -> v.candidate,
        "version" -> v.version,
        "platform" -> v.platform,
        "url" -> v.url)).head()

  def findVersion(candidate: String, version: String, platform: String): Future[Option[Version]] =
    versionsCollection
      .find(and(equal("candidate", candidate), equal("version", version), equal("platform", platform)))
      .first
      .map(doc => doc: Version)
      .toFuture
      .map(_.headOption)

  def findAllVersions(candidate: String, version: String): Future[Seq[Version]] =
    versionsCollection
      .find(and(equal("candidate", candidate), equal("version", version)))
      .map(doc => doc: Version)
      .toFuture()

}

case class Version(candidate: String, version: String, platform: String, url: String)