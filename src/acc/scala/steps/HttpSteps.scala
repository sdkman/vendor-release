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

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import steps.World._

class HttpSteps extends ScalaDsl with EN with Matchers {
  When("""^a GET request on the (.*) endpoint$""") { (endpoint: String) =>
    response = support.Http.get(endpoint)
  }

  When("""^a JSON POST on the (.*) endpoint:$""") { (endpoint: String, json: String) =>
    response = support.Http.post(endpoint, json.stripMargin)
  }

  When("""^a JSON PUT on the (.*) endpoint:$""") { (endpoint: String, payload: String) =>
    response = support.Http.put(endpoint, payload.stripMargin)
  }

  When("""^a JSON PATCH on the (.*) endpoint:$""") { (endpoint: String, payload: String) =>
    response = support.Http.patch(endpoint, payload.stripMargin)
  }

  When("""^a DELETE on the (.*) endpoint$""") { (endpoint: String) =>
    response = support.Http.delete(endpoint)
  }

  And("""^the (.*) endpoint is accessed$""") { (endpoint: String) =>
    response = support.Http.get(endpoint)
  }
}
