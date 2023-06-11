Feature: Release candidate

  Background:
    Given the consumer for candidate riot is making a request
    And the consumer has a valid auth token

  Scenario: Idempotent create and update a candidate
    When a JSON POST on the /release/candidate endpoint:
    """
          |{
          |   "id" : "riot",
          |   "name" : "Riot",
          |   "description" : "Rocking riot",
          |   "websiteUrl" : "https://riot.io",
          |   "distribution" : "UNIVERSAL"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the message "Create or update candidate: riot" is received
    And Candidate riot exists and is unique on postgres
    And Candidate riot exists and is unique on mongodb
    When a JSON POST on the /release/candidate endpoint:
    """
          |{
          |   "id" : "riot",
          |   "name" : "Riotous",
          |   "description" : "Rocking riot, forever",
          |   "websiteUrl" : "https://riotous.io",
          |   "distribution" : "UNIVERSAL"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the message "Create or update candidate: riot" is received
    And Candidate riot exists and is unique on postgres
    And Candidate riot exists and is unique on mongodb
