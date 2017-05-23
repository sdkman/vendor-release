package io.sdkman.vendor.release.repos

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers, OptionValues, WordSpec}
import support.Mongo

class VersionsRepoSpec extends WordSpec with Matchers with BeforeAndAfter with ScalaFutures with OptionValues {

  val repo = new VersionsRepo {}

  "versions repository" should {

    "attempt to find one Version by candidate, version and platform" when {

      "that version is available" in {
        val candidate = "java"
        val version = "8u111"
        val platform = "LINUX_64"
        val url = "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz"

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
  }

  before {
    Mongo.dropAllCollections()
  }
}
