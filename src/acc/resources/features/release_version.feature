#
#  Copyright 2017 Marco Vermeulen
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

Feature: Release a Candidate Version

  Background:
    Given the Client is Authorised and Authenticated as "groovy"

  Scenario: Release a Universal Candidate Version
    Given the existing Default "groovy" Version is "2.3.5"
    When a JSON POST on the "/release/universal" endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://hostname/groovy-binary-2.3.6.zip"
          |}
    """
    Then the status received is 201 "CREATED"
    And "groovy" Version "2.3.6" with URL "https://dl.bintray.com/groovy/maven/apache-groovy-binary-2.4.11.zip" was published as UNIVERSAL
    And the message "Released: groovy 2.3.6 for UNIVERSAL" is received

  Scenario: Attempt to Release a duplicate Version
    Given the existing Default "groovy" Version is "2.3.5"
    When a JSON POST on the "/release/universal" endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://hostname/groovy-binary-2.3.6.zip"
          |}
    """
    And a JSON POST on the "/release/universal" endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://hostname/groovy-binary-2.3.6.zip"
          |}
    """
    Then the status received is 409 "CONFLICT"
    And the message "Duplicate: groovy 2.3.6 already exists" is received

  Scenario: Attempt to Release a Version for a non-existent Candidate
    Given Candidate "groovy" does not exist
    When a JSON POST on the "/release/universal" endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://hostname/groovy-binary-2.3.6.zip"
          |}
    """
    Then the status received is 400 "BAD_REQUEST"
    And the message "Invalid candidate: groovy" is received
    And Candidate "groovy" Version "2.3.6" does not exists

  @pending
  Scenario: Attempt to submit malformed JSON with no Candidate
    When a JSON POST on the "/release" endpoint:
    """
          |{
          |  "version" : "2.3.6",
          |  "url" : "http://hostname/groovy-binary-2.3.6.zip"
          |}
    """
    Then the status received is "BAD_REQUEST"
    And the error message received includes "on field 'candidate': rejected value [null]"
    And the error message received includes "Candidate can not be null."

  @pending
  Scenario: Attempt to submit malformed JSON with no Version
    When a JSON POST on the "/release" endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "url" : "http://hostname/groovy-binary-2.3.6.zip"
          |}
    """
    Then the status received is "BAD_REQUEST"
    And the error message received includes "on field 'version': rejected value [null]"
    And the error message received includes "Version can not be null."

  @pending
  Scenario: Attempt to submit malformed JSON with no URL
    When a JSON POST on the "/release" endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6"
          |}
    """
    Then the status received is "BAD_REQUEST"
    And the error message received includes "on field 'url': rejected value [null]"
    And the error message received includes "URL can not be null."
