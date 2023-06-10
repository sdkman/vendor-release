package io.sdkman.vendor.release.repos

import io.sdkman.model.Version
import io.sdkman.vendor.release.PostgresConnectivity
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

trait PgVersionRepo {

  self: PostgresConnectivity =>

  def insertVersionPostgres(version: Version): Future[Int] =
    pgDatabase.run(
      sqlu"""INSERT INTO version(
              candidate,
              version,
              platform,
              visible,
              url
             ) VALUES (
                 ${version.candidate},
                 ${version.version},
                 ${version.platform},
                 ${version.visible},
                 ${version.url}
             )"""
    )

  def updateVersionPostgres(oldVersion: Version, newVersion: Version): Future[Int] =
    pgDatabase.run(
      sqlu"""UPDATE version
           SET visible = ${newVersion.visible}, url = ${newVersion.url}
           WHERE candidate = ${oldVersion.candidate}
            AND version = ${oldVersion.version}
            AND platform = ${oldVersion.platform}
        """
    )
}
