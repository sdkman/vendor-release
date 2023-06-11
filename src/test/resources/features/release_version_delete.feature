Feature: Delete release version

  Background:
    And the consumer has a valid auth token

  Scenario: An existing version is deleted permanently
    Given the consumer for candidate groovy is making a request
    And the UNIVERSAL candidate groovy with default version 2.3.7 already exists
    And an existing UNIVERSAL groovy version 2.3.7 exists
    And an existing UNIVERSAL groovy version 2.3.6 exists
    When a JSON DELETE on the /release/version endpoint:
    """
    |{
    |   "candidate": "groovy",
    |   "version": "2.3.6",
    |   "platform": "UNIVERSAL"
    |}
    """
    Then the status received is 200 OK
    And the message "Deleted: groovy 2.3.6 UNIVERSAL" is received
    And the groovy version 2.3.6 UNIVERSAL does not exist

  Scenario: A default version cannot be deleted
    Given the consumer for candidate groovy is making a request
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    And an existing UNIVERSAL groovy version 2.3.6 exists
    When a JSON DELETE on the /release/version endpoint:
    """
    |{
    |   "candidate": "groovy",
    |   "version": "2.3.6",
    |   "platform": "UNIVERSAL"
    |}
    """
    Then the status received is 409 CONFLICT
    And the message "Conflict: groovy 2.3.6 UNIVERSAL" is received
    And the groovy version 2.3.6 UNIVERSAL still exists

  Scenario: A non-existent version cannot be deleted
    Given the consumer for candidate groovy is making a request
    And the groovy version 2.3.6 UNIVERSAL does not exist
    When a JSON DELETE on the /release/version endpoint:
    """
    |{
    |   "candidate": "groovy",
    |   "version": "2.3.6",
    |   "platform": "UNIVERSAL"
    |}
    """
    Then the status received is 404 NOT_FOUND
    And the message "Not found: groovy 2.3.6 UNIVERSAL" is received
