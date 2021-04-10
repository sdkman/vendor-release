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

Feature: Default Endpoint Security

  Scenario: The Default endpoints can NOT be Accessed without a valid Auth Token
    Given the Consumer for groovy is making a request
    And the Consumer does not have a valid Auth Token
    And the Consumer does not have a valid Auth Token
    And an existing UNIVERSAL groovy Version 2.3.5 exists
    And an existing UNIVERSAL groovy Version 2.3.6 exists
    And the UNIVERSAL candidate groovy with default version 2.3.5 already exists
    When a JSON PUT on the /default/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 403 "FORBIDDEN"

  Scenario: The Default endpoints can NOT by an invalid Consumer
    Given the Consumer for scala is making a request
    And the Consumer has a valid Auth Token
    And the Consumer has a valid Auth Token
    And an existing UNIVERSAL groovy Version 2.3.5 exists
    And an existing UNIVERSAL groovy Version 2.3.6 exists
    And the UNIVERSAL candidate groovy with default version 2.3.5 already exists
    When a JSON PUT on the /default/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 403 "FORBIDDEN"

  Scenario: The Default endpoints CAN be Accessed when Authorised as valid Consumer
    Given the Consumer for groovy is making a request
    And the Consumer has a valid Auth Token
    And an existing UNIVERSAL groovy Version 2.3.5 exists
    And an existing UNIVERSAL groovy Version 2.3.6 exists
    And the existing Default UNIVERSAL groovy Version is 2.3.5
    When a JSON PUT on the /default/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 202 "ACCEPTED"

  Scenario: The Default endpoints CAN be Accessed when Authorised as Administrator
    Given the Consumer for default_admin is making a request
    And the Consumer has a valid Auth Token
    And an existing UNIVERSAL groovy Version 2.3.5 exists
    And an existing UNIVERSAL groovy Version 2.3.6 exists
    And the existing Default UNIVERSAL groovy Version is 2.3.5
    When a JSON PUT on the /default/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 202 "ACCEPTED"