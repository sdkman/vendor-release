package io.sdkman.vendor.release

import com.typesafe.config.ConfigFactory

trait Configuration {

  lazy val config = ConfigFactory.load()

  lazy val serviceHost = config.getString("service.host")

  lazy val servicePort = config.getInt("service.port")

  lazy val mongoUrl = config.getString("mongo.url")

  lazy val databaseName = config.getString("mongo.database")

  lazy val userName = config.getString("mongo.username")

  lazy val password = config.getString("mongo.password")

}
