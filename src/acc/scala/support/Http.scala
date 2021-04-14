/**
  * Copyright 2020 Marco Vermeulen
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
package support

import steps.World

import scalaj.http.{BaseHttp, HttpOptions, HttpResponse}

object Http {

  val host = "http://localhost:9000"

  def get(endpoint: String) =
    withConnectionOptions(http => http(s"$host$endpoint").headers(requiredHeaders).asString)

  def post(endpoint: String, payload: String) =
    withConnectionOptions(http => http(s"$host$endpoint").headers(requiredHeaders).postData(payload).asString)

  def put(endpoint: String, payload: String) =
    withConnectionOptions(http => http(s"$host$endpoint").headers(requiredHeaders).put(payload).asString)

  def patch(endpoint: String, payload: String) =
    withConnectionOptions(http =>
      http(s"$host$endpoint").headers(requiredHeaders).postData(payload).method("PATCH").asString
    )

  private def withConnectionOptions(f: BaseHttp => HttpResponse[String]): HttpResponse[String] =
    f(new BaseHttp(options = Seq(HttpOptions.connTimeout(1000), HttpOptions.readTimeout(5000))))

  private def requiredHeaders = Map(
    "Service-Token" -> World.token,
    "Consumer" -> World.consumer,
    "Accept" -> "application/json",
    "Content-Type" -> "application/json"
  )
}
