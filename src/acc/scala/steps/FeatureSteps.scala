/**
 * Copyright 2014 Marco Vermeulen
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

import cucumber.api.PendingException
import cucumber.api.scala.{EN, ScalaDsl}
import io.sdkman.vendor.release.JsonSupport
import org.scalatest.Matchers

class FeatureSteps extends ScalaDsl with EN with Matchers with JsonSupport {

  Then( """^the status received is (.*) "(.*)"$""") { (code: Int, status: String) =>
    World.response.code shouldBe code
  }

  Then( """^the message "(.*?)" is received$""") { (message: String) =>
    World.response.body shouldBe message
  }

  Then( """^the message containing "(.*?)" is received$""") { (message: String) =>
    World.response.body should include(message)
  }
}
