/**
  * Copyright 2023 SDKMAN!
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
import org.scalatest.matchers.should.Matchers
import steps.World._

class SecuritySteps extends ScalaDsl with EN with Matchers {

  And("""^the consumer does not have a valid auth token""") { () =>
    token = "invalid_token"
  }

  And("""^the consumer has a valid auth token""") { () =>
    token = "default_token"
  }

  And("""^the consumer for candidate (.*) is making a request$""") { (c: String) =>
    candidates = c
  }

  And("""^Vendor header '(.*)' is passed with the request$""") { (v: String) =>
    vendor = Some(v)
  }

}
