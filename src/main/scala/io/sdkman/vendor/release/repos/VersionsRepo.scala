package io.sdkman.vendor.release.repos

import io.sdkman.vendor.release.MongoConnectivity
import org.mongodb.scala.Completed
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters.{and, equal}

import scala.concurrent.Future

trait VersionsRepo extends MongoConnectivity {

  def saveVersion(v: Version): Future[Completed] =
    versionsCollection.insertOne(
      Document(
        "candidate" -> v.candidate,
        "version" -> v.version,
        "platform" -> v.platform,
        "url" -> v.url)).head()

  def findVersion(candidate: String, version: String, platform: String): Future[Option[Version]] =
    versionsCollection
      .find(and(equal("candidate", candidate), equal("version", version), equal("platform", platform)))
      .first
      .map(doc => doc: Version)
      .toFuture
      .map(_.headOption)

}

case class Version(candidate: String, version: String, platform: String, url: String)