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

Feature: Delete release version

  Background:
    And the consumer has a valid auth token

  Scenario: An existing version is deleted permanently
    Given the consumer for candidate groovy is making a request
    And the UNIVERSAL candidate groovy with default version 2.3.7 already exists
    And an existing UNIVERSAL groovy version 2.3.7 exists
    And an existing UNIVERSAL groovy version 2.3.6 exists
    When a JSON DELETE on the /release endpoint:
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
    When a JSON DELETE on the /release endpoint:
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
    When a JSON DELETE on the /release endpoint:
    """
    |{
    |   "candidate": "groovy",
    |   "version": "2.3.6",
    |   "platform": "UNIVERSAL"
    |}
    """
    Then the status received is 404 NOT_FOUND
    And the message "Not found: groovy 2.3.6 UNIVERSAL" is received
