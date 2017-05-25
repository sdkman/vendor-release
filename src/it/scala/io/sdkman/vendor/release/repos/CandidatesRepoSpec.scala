package io.sdkman.vendor.release.repos

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers, OptionValues, WordSpec}
import support.Mongo

class CandidatesRepoSpec extends WordSpec with Matchers with BeforeAndAfter with ScalaFutures with OptionValues {

  val repo = new CandidatesRepo {}

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
  }

  before {
    Mongo.dropAllCollections()
    Mongo.insertCandidates(Seq(scala, groovy, java))
  }

}
