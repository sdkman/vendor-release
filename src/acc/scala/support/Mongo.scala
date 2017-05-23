package support

import java.util.concurrent.TimeUnit

import io.sdkman.vendor.release.repos.Version
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.{MongoClient, ScalaObservable, _}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}

case class Candidate(candidate: String,
                     name: String,
                     description: String,
                     default: String,
                     websiteUrl: String,
                     distribution: String)

object Mongo {

  import Helpers._

  lazy val mongoClient = MongoClient("mongodb://mongo:27017")

  lazy val db = mongoClient.getDatabase("sdkman")

  lazy val appCollection = db.getCollection("application")

  def insertAliveOk() = appCollection.insertOne(Document("alive" -> "OK")).results()

  lazy val versionsCollection = db.getCollection("versions")

  lazy val candidatesCollection = db.getCollection("candidates")

  def insertVersions(vs: Seq[Version]) = vs.foreach(insertVersion)

  def insertVersion(v: Version) =
    versionsCollection.insertOne(
      Document(
        "candidate" -> v.candidate,
        "version" -> v.version,
        "platform" -> v.platform,
        "url" -> v.url))
      .results()

  def insertCandidates(cs: Seq[Candidate]) = cs.foreach(insertCandidate)

  def insertCandidate(c: Candidate) =
    candidatesCollection.insertOne(
      Document(
        "candidate" -> c.candidate,
        "name" -> c.name,
        "description" -> c.description,
        "default" -> c.default,
        "websiteUrl" -> c.websiteUrl,
        "distribution" -> c.distribution))
      .results()

  def candidateExists(candidate: String): Boolean = Await.result(
    candidatesCollection
      .find(and(equal("candidate", candidate)))
      .first
      .toFuture()
      .map(_.nonEmpty), 5.seconds)

  def versionExists(candidate: String, version: String): Boolean = Await.result(
    versionsCollection
      .find(and(equal("candidate", candidate), equal("version", version)))
      .first
      .toFuture()
      .map(_.nonEmpty), 5.seconds)

  def isDefault(candidate: String, version: String): Boolean = Await.result(
    candidatesCollection
      .find(and(equal("candidate", candidate), equal("version", version)))
      .first
      .toFuture()
      .map(_.nonEmpty), 5.seconds)

  def versionPublished(candidate: String, version: String, url: String, platform: String): Boolean = Await.result(
    versionsCollection
      .find(and(equal("candidate", candidate), equal("version", version), equal("platform", platform)))
      .first
      .toFuture()
      .map(_.nonEmpty), 5.seconds)

  def dropAllCollections() = {
    appCollection.drop().results()
    versionsCollection.drop().results()
    candidatesCollection.drop().results()
  }

  private def field(n: String, d: Document) = d.get[BsonString](n).map(_.asString.getValue).get
}

object Helpers {

  implicit class DocumentObservable[C](val observable: Observable[Document]) extends ImplicitObservable[Document] {
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