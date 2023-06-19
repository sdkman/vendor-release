Feature: Release candidate

  Background:
    Given the consumer for candidate riot is making a request
    And the consumer has a valid auth token

  Scenario: Idempotent create and update a candidate
    When a JSON POST on the /candidates endpoint:
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
    When a JSON POST on the /candidates endpoint:
    """
          |{
          |   "id" : "riot",
          |   "name" : "Riot",
          |   "description" : "Get data in and out of Redis with RIOT! Redis Input/Output Tools (RIOT) is a command-line utility designed to help you get data in and out of Redis.",
          |   "websiteUrl" : "https://github.com/redis-developer/riot",
          |   "distribution" : "PLATFORM_SPECIFIC"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the message "Create or update candidate: riot" is received
    And Candidate riot exists and is unique on postgres
    And Candidate riot exists and is unique on mongodb
