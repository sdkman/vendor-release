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

Feature: Default Candidate Version

  Background:
    Given the Consumer groovy is making a request
    And the Consumer has a valid Auth Token

  Scenario: Mark an existing Candidate Version as Default
    Given an existing UNIVERSAL groovy Version 2.3.5 exists
    And an existing UNIVERSAL groovy Version 2.3.6 exists
    And the existing Default UNIVERSAL groovy Version is 2.3.5
    When a JSON PUT on the /default/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the message "Defaulted: groovy 2.3.6" is received
    And the Default groovy Version has changed to 2.3.6

  Scenario: Attempt to mark a non-existent Candidate Version as Default
    Given the existing Default UNIVERSAL groovy Version is 2.3.5
    And Candidate "groovy" Version "2.3.6" does not exists
    When a JSON PUT on the /default/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 400 "BAD_REQUEST"
    And the message "Invalid candidate version: groovy 2.3.6" is received

  Scenario: Attempt to mark a non-existent Candidate Default
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

  Scenario: Attempt to mark a Non-Default Candidate Default
    Given the existing UNIVERSAL groovy Version has no Default
    And an existing UNIVERSAL groovy Version 2.3.6 exists
    When a JSON PUT on the /default/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the message "Defaulted: groovy 2.3.6" is received
    And the Default groovy Version has changed to 2.3.6


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
