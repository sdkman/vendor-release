package io.sdkman.vendor.release

import slick.jdbc.PostgresProfile.api._

trait PostgresConnectivity {
  val pgDatabase = Database.forConfig("jdbc")
}
