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
import org.mongodb.scala._
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.Future

trait CandidatesRepo extends MongoConnectivity {

  self: Configuration =>

  def findCandidate(candidate: String): Future[Option[Candidate]] =
    candidatesCollection
      .find(equal("candidate", candidate))
      .first
      .map(doc => doc: Candidate)
      .toFuture()
      .map(_.headOption)

  def updateDefaultVersion(candidate: String, version: String): Future[UpdateResult] =
    candidatesCollection
      .updateOne(equal("candidate", candidate), set("default", version))
      .head()
}

case class Candidate(candidate: String,
                     name: String,
                     description: String,
                     default: String,
                     websiteUrl: String,
                     distribution: String)
