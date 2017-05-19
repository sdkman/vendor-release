package io.sdkman.vendor.release.repos

import io.sdkman.vendor.release.MongoConnectivity
import org.mongodb.scala.{Completed, Observable}
import org.mongodb.scala.bson.collection.immutable.Document

trait VersionsRepo extends MongoConnectivity {

  def saveVersion(v: Version): Observable[Completed] = versionsCollection.insertOne(
    Document(
      "candidate" -> v.candidate,
      "version" -> v.version,
      "platform" -> v.platform,
      "url" -> v.url))

}

case class Version(candidate: String, version: String, platform: String, url: String)