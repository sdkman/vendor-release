#
#  Copyright 2020 Marco Vermeulen
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

Feature: Universal Candidate Release

  Background:
    Given the Consumer for groovy is making a request
    And the Consumer has a valid Auth Token
    And the URI /groovy-binary-2.3.6.zip is available for download

  Scenario: Release a Universal Candidate Version
    Given an existing UNIVERSAL groovy Version 2.3.5 exists
    And the existing Default UNIVERSAL groovy Version is 2.3.5
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 201 CREATED
    And groovy Version 2.3.6 with URL http://localhost:8080/groovy-2.3.6.zip was published as UNIVERSAL
    And the message "Released: groovy 2.3.6 for UNIVERSAL" is received

  Scenario: Attempt to Release a duplicate Version
    Given an existing UNIVERSAL groovy Version 2.3.5 exists
    And the existing Default UNIVERSAL groovy Version is 2.3.5
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    And a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 409 CONFLICT
    And the message "Duplicate: groovy 2.3.6 already exists" is received

  Scenario: Attempt to Release a Version for a non-existent Candidate
    Given Candidate groovy does not exist
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message "Invalid candidate: groovy" is received
    And Candidate groovy Version 2.3.6 does not exists

  Scenario: Attempt to submit malformed JSON with no Candidate
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message containing "The request content was malformed" is received
    And the message containing "Object is missing required member 'candidate'" is received

  Scenario: Attempt to submit malformed JSON with no Version
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message containing "The request content was malformed" is received
    And the message containing "Object is missing required member 'version'" is received

  Scenario: Attempt to submit malformed JSON with no URL
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message containing "The request content was malformed" is received
    And the message containing "Object is missing required member 'url'" is received
