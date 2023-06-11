/**
  * Copyright 2023 SDKMAN!
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
