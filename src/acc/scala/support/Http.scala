/**
  * Copyright 2014 Marco Vermeulen
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

import scalaj.http.{BaseHttp, HttpOptions, HttpRequest, HttpResponse, Http => HttpClient}

object Http {

  val host = "http://vendor-release:9000"

  def get(endpoint: String) = httpCallWithOptions { http =>
    http(s"$host$endpoint").headers(
      "access_token" -> World.token,
      "consumer" -> World.consumer,
      "Content-Type" -> "application/json"
    ).asString
  }

  def post(endpoint: String, payload: String) = httpCallWithOptions { http =>
    http(s"$host$endpoint").headers(
      "access_token" -> World.token,
      "consumer" -> World.consumer,
      "Accept" -> "application/json",
      "Content-Type" -> "application/json"
    ).postData(payload).asString
  }

  def put(endpoint: String, payload: String) = httpCallWithOptions { http =>
    http(s"$host$endpoint").headers(
      "access_token" -> World.token,
      "consumer" -> World.consumer,
      "Accept" -> "application/json",
      "Content-Type" -> "application/json"
    ).put(payload).asString
  }

  private def httpCallWithOptions(f: BaseHttp => HttpResponse[String]): HttpResponse[String] =
    f(new BaseHttp(options = Seq(HttpOptions.connTimeout(1000), HttpOptions.readTimeout(5000))))

}
