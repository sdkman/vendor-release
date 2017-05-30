package io.sdkman.vendor.release.repos

import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfter, Matchers, OptionValues, WordSpec}
import support.Mongo

class VersionsRepoSpec extends WordSpec with Matchers with BeforeAndAfter with ScalaFutures with OptionValues with Eventually with IntegrationPatience {

  val repo = new VersionsRepo {}

  val candidate = "java"
  val version = "8u111"
  val platform = "LINUX_64"
  val url = "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz"

  "versions repository" should {

    "persist a version" in {
      repo.saveVersion(Version(candidate, version, platform, url))

      eventually {
        Mongo.versionPublished(candidate, version, url, platform) shouldBe true
      }
    }

    "attempt to find one Version by candidate, version and platform" when {

      "that version is available" in {
        Mongo.insertVersion(Version(candidate, version, platform, url))

        whenReady(repo.findVersion(candidate, version, platform)) { maybeVersion =>
          maybeVersion.value.candidate shouldBe candidate
          maybeVersion.value.version shouldBe version
          maybeVersion.value.platform shouldBe platform
          maybeVersion.value.url shouldBe url
        }
      }

      "find no Version by candidate, version and platform" in {
        whenReady(repo.findVersion("java", "7u65", "LINUX_64")) { maybeVersion =>
          maybeVersion should not be defined
        }
      }
    }

    "attempt to find all Versions by candidate and version" when {
      "more than one version is available" in {
        Mongo.insertVersion(Version(candidate, version, "LINUX_64", url))
        Mongo.insertVersion(Version(candidate, version, "MAC_OSX", url))

        whenReady(repo.findAllVersions(candidate, version)) { versions =>
          versions shouldBe 'nonEmpty
          versions.size shouldBe 2
        }
      }
    }
  }

  before {
    Mongo.dropAllCollections()
  }
}
