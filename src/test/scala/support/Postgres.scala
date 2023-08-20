package support

import io.sdkman.model.Version
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object Postgres {

  val db = Database.forConfig("jdbc")

  def truncateTables(): Unit =
    Await.result(for {
      _ <- db.run(sqlu"TRUNCATE TABLE version")
      _ <- db.run(sqlu"TRUNCATE TABLE candidate")
    } yield (), 1 second)

  def versionPublishedWithUrl(
      candidate: String,
      version: String,
      platform: String,
      url: String
  ): Boolean =
    Await.result(
      db.run(sql"""SELECT url
              FROM version
              WHERE candidate = $candidate
                AND version = $version
                  AND platform = $platform
        """.as[String]).map(_.contains(url)),
      1 second
    )

  def versionPublishedForVendor(
      candidate: String,
      version: String,
      platform: String,
      vendor: String
  ): Boolean =
    Await.result(
      db.run(sql"""SELECT vendor
              FROM version
              WHERE candidate = $candidate
                AND version = $version
                  AND platform = $platform
        """.as[String]).map(_.contains(vendor)),
      1 second
    )

  def versionExistsAndIsUnique(candidate: String, version: String, platform: String): Boolean =
    Await
      .result(
        db.run(
          sql"""SELECT id FROM version
             WHERE candidate = $candidate
                AND version = $version
                AND platform = $platform""".as[String]
        ),
        1 second
      )
      .size == 1

  def insertVersion(version: Version): Int = Await.result(
    db.run(
      sqlu"""INSERT INTO version(candidate, version, platform, visible, url)
             VALUES (
              ${version.candidate},
              ${version.version},
              ${version.platform},
              ${version.visible},
              ${version.url}
             )"""
    ),
    1 second
  )

  def isDefault(candidate: String, version: String): Boolean =
    Await.result(
      db.run(
        sql"""SELECT default_version FROM candidate WHERE id = $candidate"""
          .as[String]
          .map(_.contains(version))
      ),
      1 second
    )

  def candidateExistsAndIsUnique(candidate: String): Boolean =
    Await
      .result(
        db.run(
          sql"""SELECT id FROM candidate WHERE id = $candidate""".as[String]
        ),
        1 second
      )
      .size == 1

}
