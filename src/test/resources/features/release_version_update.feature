Feature: Update release version

  Background:
    And the consumer has a valid auth token

  Scenario: Hide an existing visible universal candidate version
    Given the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    And an existing UNIVERSAL groovy version 2.3.6 exists
    And groovy version 2.3.6 is visible
    When a JSON PATCH on the /release/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6",
          |   "platform": "UNIVERSAL",
          |   "visible": false
          |}
    """
    Then the status received is 204 NO_CONTENT
    And groovy version 2.3.6 is hidden

  Scenario: Change the URL of an existing universal candidate version
    Given the consumer for candidate groovy is making a request
    And the URI /groovy-x.y.z.zip is available for download
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    And an existing UNIVERSAL groovy version 2.3.6 exists
    When a JSON PATCH on the /release/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6",
          |   "platform": "UNIVERSAL",
          |   "url" : "http://localhost:8080/groovy-x.y.z.zip"
          |}
    """
    Then the status received is 204 NO_CONTENT
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-x.y.z.zip was published as UNIVERSAL to mongodb

  Scenario: Hide an existing visible multi-platform candidate version
    Given the consumer for candidate java is making a request
    And the URI /zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz is available for download
    And the PLATFORM_SPECIFIC candidate java with default version 8.0.131-zulu already exists
    And an existing LINUX_64 java version 8.0.131-zulu exists
    And java version 8.0.131-zulu is visible
    When a JSON PATCH on the /release/version endpoint:
    """
          |{
          |   "candidate" : "java",
          |   "version" : "8.0.131-zulu",
          |   "platform": "LINUX_64",
          |   "visible": false
          |}
    """
    Then the status received is 204 NO_CONTENT
    And java version 8.0.131-zulu is hidden

  Scenario: Change the URL of an existing multi-platform candidate version
    Given the consumer for candidate java is making a request
    And the URI /zulu8.21.0.1-jdk8.0.141-linux_x64.tar.gz is available for download
    And the PLATFORM_SPECIFIC candidate java with default version 8.0.131-zulu already exists
    And an existing LINUX_64 java version 8.0.131-zulu exists
    When a JSON PATCH on the /release/version endpoint:
    """
          |{
          |   "candidate" : "java",
          |   "version" : "8.0.131-zulu",
          |   "platform": "LINUX_64",
          |   "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.141-linux_x64.tar.gz"
          |}
    """
    Then the status received is 204 NO_CONTENT
    And java version 8.0.131-zulu with URL http://localhost:8080/zulu8.21.0.1-jdk8.0.141-linux_x64.tar.gz was published as LINUX_64 to mongodb

    Scenario: Reject a non-existent version
      Given the consumer for candidate groovy is making a request
      And Candidate groovy does not exist
      And the groovy version 2.3.6 UNIVERSAL does not exist
      When a JSON PATCH on the /release/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6",
          |   "platform": "UNIVERSAL",
          |   "visible": false
          |}
    """
      Then the status received is 400 BAD_REQUEST
      And the message "Does not exist: groovy 2.3.6 UNIVERSAL" is received
