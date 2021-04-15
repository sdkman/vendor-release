Feature: Update a Version

  Background:
    And the Consumer has a valid Auth Token

  Scenario: Hide an existing visible universal Candidate Version
    Given the Consumer for groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    And an existing UNIVERSAL groovy Version 2.3.6 exists
    And groovy Version 2.3.6 is visible
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
    And groovy Version 2.3.6 is hidden

  Scenario: Change the URL of an existing universal Candidate Version
    Given the Consumer for groovy is making a request
    And the URI /groovy-x.y.z.zip is available for download
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    And an existing UNIVERSAL groovy Version 2.3.6 exists
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
    And groovy Version 2.3.6 with URL http://localhost:8080/groovy-x.y.z.zip was published as UNIVERSAL

  Scenario: Hide an existing visible multi-platform Candidate Version
    Given the Consumer for java is making a request
    And the URI /zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz is available for download
    And the PLATFORM_SPECIFIC candidate java with default version 8.0.131-zulu already exists
    And an existing LINUX_64 java Version 8.0.131-zulu exists
    And java Version 8.0.131-zulu is visible
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
    And java Version 8.0.131-zulu is hidden

  Scenario: Change the URL of an existing multi-platform Candidate Version
    Given the Consumer for java is making a request
    And the URI /zulu8.21.0.1-jdk8.0.141-linux_x64.tar.gz is available for download
    And the PLATFORM_SPECIFIC candidate java with default version 8.0.131-zulu already exists
    And an existing LINUX_64 java Version 8.0.131-zulu exists
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
    And java Version 8.0.131-zulu with URL http://localhost:8080/zulu8.21.0.1-jdk8.0.141-linux_x64.tar.gz was published as LINUX_64
