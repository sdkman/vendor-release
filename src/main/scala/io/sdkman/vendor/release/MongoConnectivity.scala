/**
  * Copyright 2017 Marco Vermeulen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
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

