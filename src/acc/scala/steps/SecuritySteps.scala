/**
 * Copyright 2017 Marco Vermeulen
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

class SecuritySteps extends ScalaDsl with EN with Matchers {

  And( """^the Consumer does not have a valid Auth Token""") { () =>
    token = "invalid_token"
  }

  And( """^the Consumer has a valid Auth Token""") { () =>
    token = "default_token"
  }

  And( """^the Consumer (.*) is making a request$""") { (c: String) =>
    consumer = c
  }

}