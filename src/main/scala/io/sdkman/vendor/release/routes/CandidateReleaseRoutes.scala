package io.sdkman.vendor.release.routes

import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.LazyLogging
import io.sdkman.db.{MongoConfiguration, MongoConnectivity}
import io.sdkman.model._
import io.sdkman.repos.CandidatesRepo
import io.sdkman.vendor.release.{Configuration, HttpResponses, PostgresConnectivity}
import io.sdkman.vendor.release.repos.PgCandidateRepo
import org.mongodb.scala.Completed
import org.mongodb.scala.model.Filters.equal

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CandidateReleaseRoutes
    extends Directives
    with Configuration
    with PostgresConnectivity
    with MongoConfiguration
    with MongoConnectivity
    with PgCandidateRepo
    with CandidatesRepo
    with LazyLogging
    with JsonSupport
    with HttpResponses
    with Authorisation {

  val candidateReleaseRoutes: Route = path("candidates") {
    post {
      entity(as[PostCandidateReleaseRequest]) { req =>
        authorised(req.id) {
          complete {
            for {
              _ <- insertCandidatePostgres(
                req.id,
                req.name,
                req.description,
                req.websiteUrl,
                req.distribution
              )
              candidate = Candidate(
                candidate = req.id,
                name = req.name,
                description = req.description,
                websiteUrl = req.websiteUrl,
                distribution = req.distribution,
                default = None
              )
              _ <- upsertCandidate(candidate)
            } yield acceptedResponse(s"Create or update candidate: ${req.id}")
          }
        }
      }
    }
  }

  private def upsertCandidate(candidate: Candidate): Future[Completed] = {
    for {
      _      <- candidatesCollection.deleteOne(equal("candidate", candidate.candidate)).toFuture()
      result <- insertCandidate(candidate)
    } yield result
  }
}
