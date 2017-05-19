package io.sdkman.vendor.release

import com.mongodb.ConnectionString
import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoCredential}
import org.mongodb.scala.connection.ClusterSettings

import scala.collection.JavaConverters._

trait MongoConnectivity extends Configuration {

  def credential = MongoCredential.createCredential(userName, databaseName, password.toCharArray)

  lazy val clusterSettings = ClusterSettings.builder()
    .applyConnectionString(new ConnectionString(mongoUrl))
    .build()

  lazy val clientSettings = MongoClientSettings.builder()
    .credentialList(List(credential).asJava)
    .clusterSettings(clusterSettings)
    .build()

  lazy val mongoClient = if (userName.isEmpty) MongoClient(mongoUrl) else MongoClient(clientSettings)

  def db = mongoClient.getDatabase("sdkman")

  def appCollection = db.getCollection("application")

  def versionsCollection = db.getCollection("versions")

  def candidatesCollection = db.getCollection("candidates")
}

