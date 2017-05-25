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
package io.sdkman.vendor.release

import java.util.concurrent.Executors

import org.mongodb.scala.bson.{BsonString, Document}

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

package object repos {

  implicit val mongoExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

  implicit def documentToCandidate(doc: Document): Candidate =
    Candidate(
      field("candidate", doc),
      field("name", doc),
      field("description", doc),
      field("default", doc),
      field("websiteUrl", doc),
      field("distribution", doc))

  implicit def documentToVersion(doc: Document): Version =
    Version(
      field("candidate", doc),
      field("version", doc),
      field("platform", doc),
      field("url", doc))

  private def field(n: String, d: Document) = d.get[BsonString](n).map(_.asString.getValue).get
}