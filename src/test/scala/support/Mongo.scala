package support

import java.util.concurrent.TimeUnit

import io.sdkman.repos.{Application, Candidate, Version}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.{MongoClient, ScalaObservable, _}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Mongo {

  import Helpers._

  val codecRegistry = fromRegistries(
    fromProviders(classOf[Version], classOf[Candidate], classOf[Application]),
    DEFAULT_CODEC_REGISTRY
  )

  lazy val mongoClient = MongoClient("mongodb://localhost:27017")

  lazy val db = mongoClient.getDatabase("sdkman").withCodecRegistry(codecRegistry)

  lazy val appCollection: MongoCollection[Application] = db.getCollection("application")

  def insertAliveOk() = appCollection.insertOne(Application("OK", "", "")).results()

  lazy val versionsCollection: MongoCollection[Version] = db.getCollection("versions")

  lazy val candidatesCollection: MongoCollection[Candidate] = db.getCollection("candidates")

  def insertVersions(vs: Seq[Version]) = versionsCollection.insertMany(vs).results()

  def insertVersion(v: Version) = versionsCollection.insertOne(v).results()

  def insertCandidates(cs: Seq[Candidate]) = candidatesCollection.insertMany(cs).results()

  def insertCandidate(c: Candidate) = candidatesCollection.insertOne(c).results()

  def candidateExists(candidate: String): Boolean =
    candidatesCollection.find(equal("candidate", candidate)).results().nonEmpty

  def versionExists(candidate: String, version: String, platform: String): Boolean =
    versionsCollection
      .find(
        and(equal("candidate", candidate), equal("version", version), equal("platform", platform))
      )
      .results()
      .nonEmpty

  def isDefault(candidate: String, version: String): Boolean =
    candidatesCollection
      .find(and(equal("candidate", candidate), equal("default", version)))
      .results()
      .nonEmpty

  def versionVisible(candidate: String, version: String): Boolean =
    versionsCollection
      .find(and(equal("candidate", candidate), equal("version", version), equal("visible", true)))
      .results()
      .nonEmpty

  def versionPublished(candidate: String, version: String, url: String, platform: String): Boolean =
    versionsCollection
      .find(
        and(
          equal("candidate", candidate),
          equal("version", version),
          equal("platform", platform),
          equal("url", url)
        )
      )
      .results()
      .nonEmpty

  def versionVendor(candidate: String, version: String, platform: String): Option[String] =
    versionsCollection
      .find(
        and(equal("candidate", candidate), equal("version", version), equal("platform", platform))
      )
      .results()
      .flatMap(_.vendor)
      .headOption

  def dropAllCollections() = {
    appCollection.drop().results()
    versionsCollection.drop().results()
    candidatesCollection.drop().results()
  }
}

object Helpers {

  implicit class DocumentObservable[C](val observable: Observable[Document])
      extends ImplicitObservable[Document] {
    override val converter: (Document) => String = (doc) => doc.toJson
  }

  implicit class GenericObservable[C](val observable: Observable[C]) extends ImplicitObservable[C] {
    override val converter: (C) => String = (doc) => doc.toString
  }

  trait ImplicitObservable[C] {
    val observable: Observable[C]
    val converter: (C) => String

    def results(): Seq[C] = Await.result(observable.toFuture(), Duration(10, TimeUnit.SECONDS))

    def headResult() = Await.result(observable.head(), Duration(10, TimeUnit.SECONDS))

    def printResults(initial: String = ""): Unit = {
      if (initial.length > 0) print(initial)
      results().foreach(res => println(converter(res)))
    }

    def printHeadResult(initial: String = ""): Unit = println(s"$initial${converter(headResult())}")
  }

}
