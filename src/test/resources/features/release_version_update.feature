#
#  Copyright 2023 SDKMAN!
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

Feature: Update release version

  Background:
    And the consumer has a valid auth token

  Scenario: Hide an existing visible universal candidate version
    Given the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    And an existing UNIVERSAL groovy version 2.3.6 exists
    And groovy version 2.3.6 is visible
    When a JSON PATCH on the /versions endpoint:
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
    When a JSON PATCH on the /versions endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6",
          |   "platform": "UNIVERSAL",
          |   "url" : "http://localhost:8080/groovy-x.y.z.zip"
          |}
    """
    Then the status received is 204 NO_CONTENT
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-x.y.z.zip was published for UNIVERSAL to mongodb

  Scenario: Hide an existing visible multi-platform candidate version
    Given the consumer for candidate java is making a request
    And the URI /zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz is available for download
    And the PLATFORM_SPECIFIC candidate java with default version 8.0.131-zulu already exists
    And an existing LINUX_64 java version 8.0.131-zulu exists
    And java version 8.0.131-zulu is visible
    When a JSON PATCH on the /versions endpoint:
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
    When a JSON PATCH on the /versions endpoint:
    """
          |{
          |   "candidate" : "java",
          |   "version" : "8.0.131-zulu",
          |   "platform": "LINUX_64",
          |   "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.141-linux_x64.tar.gz"
          |}
    """
    Then the status received is 204 NO_CONTENT
    And java version 8.0.131-zulu with URL http://localhost:8080/zulu8.21.0.1-jdk8.0.141-linux_x64.tar.gz was published for LINUX_64 to mongodb

  Scenario: Change the vendor of an existing multi-platform candidate version
    Given the consumer for candidate java is making a request
    And the URI /zulu8.21.0.1-jdk8.0.141-linux_x64.tar.gz is available for download
    And the PLATFORM_SPECIFIC candidate java with default version 8.0.131-zulu already exists
    And an existing LINUX_64 java version 8.0.131-zulu exists
    When a JSON PATCH on the /versions endpoint:
    """
          |{
          |   "candidate" : "java",
          |   "version" : "8.0.131-zulu",
          |   "platform": "LINUX_64",
          |   "vendor" : "zulu"
          |}
    """
    Then the status received is 204 NO_CONTENT
    And java version 8.0.131-zulu for vendor zulu was published for LINUX_64 to mongodb

    Scenario: Reject a non-existent version
      Given the consumer for candidate groovy is making a request
      And Candidate groovy does not exist
      And the groovy version 2.3.6 UNIVERSAL does not exist on mongodb
      When a JSON PATCH on the /versions endpoint:
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
