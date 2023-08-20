package support

import java.util.concurrent.TimeUnit

import io.sdkman.model.{Application, Candidate, Version}
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

  private val codecRegistry = fromRegistries(
    fromProviders(classOf[Version], classOf[Candidate], classOf[Application]),
    DEFAULT_CODEC_REGISTRY
  )

  private lazy val mongoClient = MongoClient("mongodb://localhost:27017")

  private lazy val db = mongoClient.getDatabase("sdkman").withCodecRegistry(codecRegistry)

  lazy val appCollection: MongoCollection[Application] = db.getCollection("application")

  def insertAliveOk(): Seq[Completed] = appCollection.insertOne(Application("OK", "", "")).results()

  private lazy val versionsCollection: MongoCollection[Version] = db.getCollection("versions")

  private lazy val candidatesCollection: MongoCollection[Candidate] = db.getCollection("candidates")

  def insertVersions(vs: Seq[Version]): Seq[Completed] = versionsCollection.insertMany(vs).results()

  def insertVersion(v: Version): Seq[Completed] = versionsCollection.insertOne(v).results()

  def insertCandidates(cs: Seq[Candidate]): Seq[Completed] =
    candidatesCollection.insertMany(cs).results()

  def insertCandidate(c: Candidate): Seq[Completed] = candidatesCollection.insertOne(c).results()

  def candidateExistsAndIsUnique(candidate: String): Boolean =
    candidatesCollection.find(equal("candidate", candidate)).results().size == 1

  def versionExistsAndIsUnique(candidate: String, version: String, platform: String): Boolean =
    versionsCollection
      .find(
        and(equal("candidate", candidate), equal("version", version), equal("platform", platform))
      )
      .results()
      .size == 1

  def checksumExists(
      candidate: String,
      version: String,
      platform: String,
      algorithm: String,
      checksum: String
  ): Boolean =
    versionsCollection
      .find(
        and(equal("candidate", candidate), equal("version", version), equal("platform", platform))
      )
      .first()
      .map { v =>
        v.checksums
      }
      .filter { c =>
        c.get(algorithm) == checksum
      }
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

  def versionPublishedWithUrl(
      candidate: String,
      version: String,
      platform: String,
      url: String
  ): Boolean =
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

  def versionPublishedForVendor(
      candidate: String,
      version: String,
      platform: String,
      vendor: String
  ): Boolean =
    versionsCollection
      .find(
        and(
          equal("candidate", candidate),
          equal("version", version),
          equal("platform", platform),
          equal("vendor", vendor)
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

  def dropAllCollections(): Seq[Completed] = {
    appCollection.drop().results()
    versionsCollection.drop().results()
    candidatesCollection.drop().results()
  }
}

object Helpers {

  implicit class DocumentObservable[C](val observable: Observable[Document])
      extends ImplicitObservable[Document] {
    override val converter: Document => String = doc => doc.toJson
  }

  implicit class GenericObservable[C](val observable: Observable[C]) extends ImplicitObservable[C] {
    override val converter: C => String = doc => doc.toString
  }

  trait ImplicitObservable[C] {
    val observable: Observable[C]
    val converter: C => String

    def results(): Seq[C] = Await.result(observable.toFuture(), Duration(10, TimeUnit.SECONDS))

    def headResult(): C = Await.result(observable.head(), Duration(10, TimeUnit.SECONDS))

    def printResults(initial: String = ""): Unit = {
      if (initial.nonEmpty) print(initial)
      results().foreach(res => println(converter(res)))
    }

    def printHeadResult(initial: String = ""): Unit = println(s"$initial${converter(headResult())}")
  }

}
