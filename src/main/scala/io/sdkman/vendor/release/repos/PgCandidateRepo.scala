package io.sdkman.vendor.release.repos

import io.sdkman.vendor.release.PostgresConnectivity
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

trait PgCandidateRepo {

  self: PostgresConnectivity =>

  def updateDefaultVersionPostgres(c: String, v: String): Future[Int] =
    pgDatabase.run(
      sqlu"UPDATE candidate SET default_version = $v WHERE id = $c"
    )
}
