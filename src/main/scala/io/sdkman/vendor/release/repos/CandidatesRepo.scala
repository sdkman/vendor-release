package io.sdkman.vendor.release.repos

import io.sdkman.vendor.release.MongoConnectivity
import org.mongodb.scala._
import org.mongodb.scala.model.Filters.equal

import scala.concurrent.Future

trait CandidatesRepo extends MongoConnectivity {

  def findCandidate(candidate: String): Future[Option[Candidate]] =
    candidatesCollection
      .find(equal("candidate", candidate))
      .first
      .map(doc => doc: Candidate)
      .toFuture()
      .map(_.headOption)
}

case class Candidate(candidate: String,
                     name: String,
                     description: String,
                     default: String,
                     websiteUrl: String,
                     distribution: String)
