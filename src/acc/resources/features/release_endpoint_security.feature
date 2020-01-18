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

Feature: Release Endpoint Security

  Background:
    Given the URI /groovy-2.3.6.zip is available for download

  Scenario: The Release endpoints can NOT be Accessed without a valid Auth Token
    Given the Consumer groovy is making a request
    And the Consumer does not have a valid Auth Token
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 403 "FORBIDDEN"

  Scenario: The Release endpoints can NOT be Accessed by an invalid Consumer
    Given the Consumer scala is making a request
    And the Consumer has a valid Auth Token
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 403 "FORBIDDEN"

  Scenario: The Release endpoints CAN be Accessed when Authorised as valid Consumer
    Given the Consumer groovy is making a request
    And the Consumer has a valid Auth Token
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 201 "CREATED"

  Scenario: The Release endpoints CAN be Accessed when Authorised as Administrator
    Given the Consumer default_admin is making a request
    And the Consumer has a valid Auth Token
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 201 "CREATED"