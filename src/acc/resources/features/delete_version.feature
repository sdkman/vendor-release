Feature: Delete Version

  Background:
    And the Consumer has a valid Auth Token

  Scenario: An existing Version is deleted permanently
    Given the Consumer for groovy is making a request
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    And an existing UNIVERSAL groovy Version 2.3.6 exists
    When a DELETE on the /release/version/groovy/2.3.6/UNIVERSAL endpoint
    Then the status received is 200 OK
    And the message "Deleted: groovy 2.3.6 UNIVERSAL" is received
    And the groovy version 2.3.6 UNIVERSAL does not exist

  Scenario: A non-existent Version cannot be deleted
    Given the groovy version 2.3.6 UNIVERSAL does not exist
    When a DELETE on the /release/version/groovy/2.3.6/UNIVERSAL endpoint
    Then the status received is 404 NOT_FOUND
    And the message "Not found: groovy 2.3.6 UNIVERSAL" is received
