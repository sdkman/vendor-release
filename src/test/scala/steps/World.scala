/**
  * Copyright 2020 Marco Vermeulen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package steps

import scalaj.http.{HttpRequest, HttpResponse}

object World {
  var request: HttpRequest           = _
  var response: HttpResponse[String] = _

  var consumer: String = "invalid_consumer"
  var token: String    = "invalid_token"

  var vendor: Option[String] = None

  def reset(): Unit = {
    request = null
    response = null
    consumer = "invalid_consumer"
    token = "invalid_token"
    vendor = None
  }
}
