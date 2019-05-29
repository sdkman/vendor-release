/**
  * Copyright 2018 Marco Vermeulen
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

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import io.sdkman.db.{MongoConfiguration, MongoConnectivity}
import io.sdkman.vendor.release.Configuration

import scala.concurrent.ExecutionContext.Implicits.global

trait HealthRoutes extends Directives with Configuration with MongoConnectivity with MongoConfiguration with LazyLogging {
  val healthRoutes = path("alive") {
    get {
      complete {
        appCollection.find().headOption.map { maybeApp =>
          maybeApp.fold(HttpResponse(ServiceUnavailable)) { app =>
            logger.info(s"/alive 200 response: ${app.alive}")
            HttpResponse(OK, entity = ByteString(app.alive))
          }
        }.recover {
          case e =>
            logger.error(s"/alive 503 response ${e.getMessage}")
            HttpResponse(ServiceUnavailable, entity = ByteString(e.getMessage))
        }
      }
    }
  }
}