package support

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object Postgres {

  val db = Database.forConfig("jdbc")

  def truncateVersion(): Int = Await.result(db.run(sqlu"TRUNCATE TABLE version"), 1 second)

  def versionPublished(candidate: String, version: String, url: String, platform: String): Boolean =
    Await.result(
      db.run(sql"""SELECT url
              FROM version
              WHERE candidate = $candidate
                AND version = $version
                  AND platform = $platform
        """.as[String]).map(_.contains(url)),
      1 second
    )

  def isDefault(candidate: String, version: String): Boolean =
    Await.result(
      db.run(
        sql"""SELECT default_version FROM candidate where id = $candidate"""
          .as[String]
          .map(_.contains(version))
      ),
      1 second
    )
}
