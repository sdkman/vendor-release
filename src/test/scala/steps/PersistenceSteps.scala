/**
  * Copyright 2023 SDKMAN!
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
import io.sdkman.model.{Candidate, Version}
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import support.{Mongo, Postgres}

class PersistenceSteps extends ScalaDsl with EN with Matchers with OptionValues {

  Then("""^(.*) version (.*) with URL (.*) was published for (.*) to (.*)$""") {
    (candidate: String, version: String, url: String, platform: String, datastore: String) =>
      withClue(s"Version was not published to $datastore") {
        datastore match {
          case "postgres" =>
            Postgres.versionPublishedWithUrl(candidate, version, platform, url) shouldBe true
          case "mongodb" =>
            Mongo.versionPublishedWithUrl(candidate, version, platform, url) shouldBe true
        }
      }
  }

  Then("""^(.*) version (.*) for vendor (.*) was published for (.*) to (.*)$""") {
    (candidate: String, version: String, vendor: String, platform: String, datastore: String) =>
      withClue(s"Version was not published to $datastore") {
        datastore match {
          case "postgres" =>
            Postgres.versionPublishedForVendor(candidate, version, platform, vendor) shouldBe true
          case "mongodb" =>
            Mongo.versionPublishedForVendor(candidate, version, platform, vendor) shouldBe true
        }
      }
  }


  Then("""^the (.*) (.*) version (.*) has a vendor of '(.*)'$""") {
    (platform: String, candidate: String, version: String, vendor: String) =>
      withClue(s"Vendor does not match $vendor") {
        Mongo.versionVendor(candidate, version, platform).value shouldBe vendor
      }
  }

  Then("""^the (.*) (.*) version (.*) has no vendor$""") {
    (platform: String, candidate: String, version: String) =>
      withClue(s"Should not have a vendor") {
        Mongo.versionVendor(candidate, version, platform) shouldBe None
      }
  }

  Then("""^(.*) version (.*) is hidden""") { (candidate: String, version: String) =>
    withClue("Version is not hidden") {
      Mongo.versionVisible(candidate, version) shouldBe false
    }
  }

  Then("""^(.*) version (.*) is visible$""") { (candidate: String, version: String) =>
    withClue("Version is not hidden") {
      Mongo.versionVisible(candidate, version) shouldBe true
    }
  }

  Given("""^a (.*) (.*) version (.*) with URL (.*) already exists$""") {
    (platform: String, candidate: String, version: String, url: String) =>
      Mongo.insertVersion(
        Version(candidate, version, platform, s"http://somecandidate.org/$candidate/$version")
      )
  }

  Given("""^an existing (.*) (.*) version (.*) exists""") {
    (platform: String, candidate: String, version: String) =>
      val v = Version(
        candidate = candidate,
        version = version,
        platform = platform,
        url = s"http://somecandidate.org/$candidate/$version",
        visible = Some(true)
      )
      Mongo.insertVersion(v)
      Postgres.insertVersion(v)
  }

  Given("""^the (.*) candidate (.*) with default version (.*) already exists$""") {
    (platform: String, candidate: String, version: String) =>
      Mongo.insertCandidate(
        Candidate(
          candidate = candidate,
          name = candidate.capitalize,
          description = s"$candidate description",
          default = Some(version),
          websiteUrl = s"http://somecandidate.org/$candidate",
          distribution = platform
        )
      )
  }

  Given("""^the existing default (.*) (.*) version is (.*)$""") {
    (platform: String, candidate: String, version: String) =>
      Mongo.insertCandidate(
        Candidate(
          candidate = candidate,
          name = candidate.capitalize,
          description = s"$candidate description",
          default = Some(version),
          websiteUrl = s"http://somecandidate.org/$candidate",
          distribution = platform
        )
      )
  }

  Given("""^the existing (.*) (.*) version has no default$""") {
    (platform: String, candidate: String) =>
      Mongo.insertCandidate(
        Candidate(
          candidate = candidate,
          name = candidate.capitalize,
          description = s"$candidate description",
          default = None,
          websiteUrl = s"http://somecandidate.org/$candidate",
          distribution = platform
        )
      )
  }

  Then("""^the default (.*) version is (.*) on (.*)$""") {
    (candidate: String, version: String, datastore: String) =>
      withClue(s"The default $candidate version was not changed to $version on $datastore") {
        datastore match {
          case "postgres" =>
            Postgres.isDefault(candidate, version)
          case "mongodb" =>
            Mongo.isDefault(candidate, version) shouldBe true
        }
      }
  }

  Given("""^the (.*) version (.*) (.*) does not exist on (.*)$""") {
    (candidate: String, version: String, platform: String, datastore: String) =>
      withClue(s"$candidate $version does not exist") {
        datastore match {
          case "postgres" =>
            Postgres.versionExistsAndIsUnique(candidate, version, platform) shouldBe false
          case "mongodb" =>
            Mongo.versionExistsAndIsUnique(candidate, version, platform) shouldBe false
        }
      }
  }

  Given("""^the (.*) version (.*) (.*) uniquely exists on (.*)$""") {
    (candidate: String, version: String, platform: String, datastore: String) =>
      withClue(s"$candidate $version still exists") {
        datastore match {
          case "postgres" =>
            Postgres.versionExistsAndIsUnique(candidate, version, platform) shouldBe true
          case "mongodb" =>
            Mongo.versionExistsAndIsUnique(candidate, version, platform) shouldBe true
        }
      }
  }

  Given("""^Candidate (.*) does not exist$""") { candidate: String =>
    withClue(s"The exists: $candidate") {
      Mongo.candidateExistsAndIsUnique(candidate) shouldBe false
    }
  }

  Given("""^Candidate (.*) exists and is unique on (.*)$""") {
    (candidate: String, datastore: String) =>
      withClue(s"The candidate $candidate does not exist on $datastore") {
        datastore match {
          case "postgres" =>
            Postgres.candidateExistsAndIsUnique(candidate) shouldBe true
          case "mongodb" =>
            Mongo.candidateExistsAndIsUnique(candidate) shouldBe true
        }
      }
  }

  Given("""^an alive OK entry in the application collection$""") { () =>
    Mongo.insertAliveOk()
  }

  Then("""^(.*) version (.*) on platform (.*) has a checksum \"(.*)\" using algorithm (.*)$""") {
    (candidate: String, version: String, platform: String, checksum: String, algorithm: String) =>
      withClue("Checksum not found") {
        Mongo.checksumExists(candidate, version, platform, algorithm, checksum) shouldBe true
      }
  }
}
