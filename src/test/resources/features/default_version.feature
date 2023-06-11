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

Feature: Default candidate version

  Background:
    Given the consumer for candidate groovy is making a request
    And the consumer has a valid auth token

  Scenario: Mark an existing candidate version as default
    Given an existing UNIVERSAL groovy version 2.3.5 exists
    And an existing UNIVERSAL groovy version 2.3.6 exists
    And the existing default UNIVERSAL groovy version is 2.3.5
    When a JSON PUT on the /default/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the message "Defaulted: groovy 2.3.6" is received
    And the default groovy version is 2.3.6 on mongodb
    And the default groovy version is 2.3.6 on postgres

  Scenario: Attempt to mark a non-existent candidate version as default
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And the groovy version 2.3.6 UNIVERSAL does not exist
    When a JSON PUT on the /default/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 400 "BAD_REQUEST"
    And the message "Invalid candidate version: groovy 2.3.6" is received

  Scenario: attempt to mark a non-existent candidate default
    Given Candidate "groovy" does not exist
    When a JSON PUT on the /default/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 400 "BAD_REQUEST"
    And the message "Invalid candidate: groovy" is received

  Scenario: Attempt to mark a non-default candidate default
    Given the existing UNIVERSAL groovy version has no default
    And an existing UNIVERSAL groovy version 2.3.6 exists
    When a JSON PUT on the /default/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the message "Defaulted: groovy 2.3.6" is received
    And the default groovy version is 2.3.6 on mongodb
    And the default groovy version is 2.3.6 on postgres


  Scenario: Attempt to submit malformed JSON with no candidate
    When a JSON PUT on the /default/version endpoint:
    """
          |{
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 400 "BAD_REQUEST"
    And the message containing "The request content was malformed" is received
    And the message containing "Object is missing required member 'candidate'" is received

  Scenario: Attempt to submit malformed JSON with no default version
    When a JSON PUT on the /default/version endpoint:
    """
          |{
          |   "candidate" : "groovy"
          |}
    """
    Then the status received is 400 "BAD_REQUEST"
    And the message containing "The request content was malformed" is received
    And the message containing "Object is missing required member 'version'" is received
