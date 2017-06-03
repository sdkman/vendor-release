package io.sdkman.vendor.release.repos

import io.sdkman.vendor.release.Configuration
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers, OptionValues, WordSpec}
import support.Mongo
import support.Mongo.isDefault

class CandidatesRepoSpec extends WordSpec with Matchers with BeforeAndAfter with ScalaFutures with OptionValues {

  val repo = new CandidatesRepo with Configuration {}

  val scala = Candidate("scala", "Scala", "The Scala Language", "2.12.0", "http://www.scala-lang.org/", "UNIVERSAL")
  val groovy = Candidate("groovy", "Groovy", "The Groovy Language", "2.4.7", "http://www.groovy-lang.org/", "UNIVERSAL")
  val java = Candidate("java", "Java", "The Java Language", "8u111", "https://www.oracle.com", "MULTI_PLATFORM")

  "candidates repository" should {

    "find some single candidate when searching by know candidate identifier" in {
      val candidate = "java"
      whenReady(repo.findCandidate(candidate)) { maybeCandidate =>
        maybeCandidate.value.candidate shouldBe candidate
      }
    }

    "find none when searching by unknown candidate identifier" in {
      val candidate = "scoobeedoo"
      whenReady(repo.findCandidate(candidate)) { maybeCandidate =>
        maybeCandidate shouldNot be(defined)
      }
    }

    "update a single candidate when present" in {
      val candidate = "scala"
      val version = "2.12.1"
      whenReady(repo.updateDefaultVersion(candidate, version)) { _ =>
        withClue(s"$candidate was not set to default $version") {
          isDefault(candidate, version) shouldBe true
        }
      }
    }
  }

  before {
    Mongo.dropAllCollections()
    Mongo.insertCandidates(Seq(scala, groovy, java))
  }

}
