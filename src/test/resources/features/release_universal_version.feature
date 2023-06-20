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

Feature: Release universal version

  Background:
    Given the consumer for candidate groovy is making a request
    And the consumer has a valid auth token
    And the URI /groovy-2.3.6.zip is available for download

  Scenario: Release a universal candidate version
    Given an existing UNIVERSAL groovy version 2.3.5 exists
    And the existing default UNIVERSAL groovy version is 2.3.5
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "platform": "UNIVERSAL",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 201 CREATED
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-2.3.6.zip was published as UNIVERSAL to mongodb
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-2.3.6.zip was published as UNIVERSAL to postgres
    And the message "Released: groovy 2.3.6 for UNIVERSAL" is received

  Scenario: Overwrite an existing candidate version
    Given an existing UNIVERSAL groovy version 2.3.6 exists
    And the existing default UNIVERSAL groovy version is 2.3.6
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "platform" : "UNIVERSAL",
          |  "url" : "http://localhost:8080/groovy-x.y.z.zip"
          |}
    """
    Then the status received is 201 CREATED
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-x.y.z.zip was published as UNIVERSAL to mongodb
    And the groovy version 2.3.6 UNIVERSAL uniquely exists on mongodb
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-x.y.z.zip was published as UNIVERSAL to postgres
    And the groovy version 2.3.6 UNIVERSAL uniquely exists on postgres
    And the message "Released: groovy 2.3.6 for UNIVERSAL" is received

  Scenario: Attempt to release a version for a non-existent candidate
    Given Candidate groovy does not exist
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "platform" : "UNIVERSAL",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message "Invalid candidate: groovy" is received
    And the groovy version 2.3.6 UNIVERSAL does not exist on mongodb
    And the groovy version 2.3.6 UNIVERSAL does not exist on postgres

  Scenario: Attempt to submit malformed JSON with no candidate
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip",
          |  "platform" : "UNIVERSAL"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message containing "The request content was malformed" is received
    And the message containing "Object is missing required member 'candidate'" is received

  Scenario: Attempt to submit malformed JSON with no version
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip",
          |  "platform" : "UNIVERSAL"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message containing "The request content was malformed" is received
    And the message containing "Object is missing required member 'version'" is received

  Scenario: Attempt to submit malformed JSON with no URL
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "platform" : "UNIVERSAL"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message containing "The request content was malformed" is received
    And the message containing "Object is missing required member 'url'" is received

  Scenario: Omit the platform to infer UNIVERSAL
    Given an existing UNIVERSAL groovy version 2.3.5 exists
    And the existing default UNIVERSAL groovy version is 2.3.5
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 201 CREATED
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-2.3.6.zip was published as UNIVERSAL to mongodb
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-2.3.6.zip was published as UNIVERSAL to postgres
    And the message "Released: groovy 2.3.6 for UNIVERSAL" is received
