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
package steps

import cucumber.api.scala.{EN, ScalaDsl}
import io.sdkman.vendor.release.repos.{Candidate, Version}
import org.scalatest.Matchers
import support.Mongo

class PersistenceSteps extends ScalaDsl with EN with Matchers {

  Then( """^(.*) Version (.*) with URL (.*) was published as (.*)$""") { (candidate: String, version: String, url: String, platform: String) =>
    withClue("Version was not published") {
      Mongo.versionPublished(candidate, version, url, platform) shouldBe true
    }
  }

  Given( """^a (.*) (.*) Version (.*) with URL (.*) already exists$""") { (platform: String, candidate: String, version: String, url: String) =>
    Mongo.insertVersion(Version(candidate, version, platform, s"http://somecandidate.org/$candidate/$version"))
  }

  Given( """^an existing (.*) (.*) Version (.*) exists""") { (platform: String, candidate: String, version: String) =>
    Mongo.insertVersion(
      Version(
        candidate = candidate,
        version = version,
        platform = platform,
        url = s"http://somecandidate.org/$candidate/$version"))
  }

  Given( """^the existing Default (.*) (.*) Version is (.*)$""") { (platform: String, candidate: String, version: String) =>
    Mongo.insertCandidate(
      Candidate(
        candidate = candidate,
        name = candidate.capitalize,
        description = s"$candidate description",
        default = version,
        websiteUrl = s"http://somecandidate.org/$candidate",
        distribution = platform))
  }

  Then( """^the Default (.*) Version has changed to (.*)$""") { (candidate: String, version: String) =>
    withClue(s"The default $candidate version was not changed to $version") {
      Mongo.isDefault(candidate, version) shouldBe true
    }
  }

  Given( """^Candidate (.*) Version (.*) does not exists$""") { (candidate: String, version: String) =>
    withClue(s"$candidate $version does not exist") {
      Mongo.versionExists(candidate, version) shouldBe false
    }
  }

  Given( """^Candidate (.*) does not exist$""") { (candidate: String) =>
    withClue(s"The exists: $candidate") {
      Mongo.candidateExists(candidate) shouldBe false
    }
  }

  Given("""^an alive OK entry in the application collection$""") { () =>
    Mongo.insertAliveOk()
  }
}
