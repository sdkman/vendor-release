package io.sdkman.vendor.release.repos

import io.sdkman.model.Version
import io.sdkman.vendor.release.PostgresConnectivity
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

trait PgVersionRepo {

  self: PostgresConnectivity =>

  def insertPostgres(version: Version): Future[Int] =
    pgDatabase.run(
      sqlu"""INSERT INTO version(
                  candidate,
                  version,
                  platform,
                  visible, url
             ) VALUES (
                 ${version.candidate},
                 ${version.version},
                 ${version.platform},
                 ${version.visible},
                 ${version.url}
             )"""
    )
}
