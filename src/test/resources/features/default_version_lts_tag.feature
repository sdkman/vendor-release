#
#  Copyright 2026 SDKMAN!
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

Feature: Dual-write default version as lts tag on PUT /candidates/default

  Path 1 of the default-version lts-tag dual write: setting a non-java candidate's default
  version asserts the `lts` tag on every Mongo platform row of (candidate, version) via the
  append-only State API POST /versions/tags endpoint. The tag write is best-effort — it never
  blocks or fails the authoritative Mongo write (202 ACCEPTED), one platform's failure does not
  suppress its siblings, and java is skipped entirely. The overwhelmingly common UNIVERSAL case
  collapses to a single tag write.

  Background:
    Given the consumer has a valid auth token
    And the state API is available

  Scenario: Setting a non-java default asserts the lts tag on the UNIVERSAL platform row
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And an existing UNIVERSAL groovy version 2.3.6 exists
    And the consumer for candidate groovy is making a request
    When a JSON PUT on the /candidates/default endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the default groovy version is 2.3.6 on mongodb
    And the state API received a POST /versions/tags with tag lts for groovy 2.3.6 UNIVERSAL
    And the state API received a POST /versions/tags with a Bearer token
    And the state API received 1 POST /versions/tags requests

  Scenario: A multi-platform non-java default asserts the lts tag once per platform row
    Given the existing default LINUX_64 groovy version is 2.3.5
    And an existing LINUX_64 groovy version 2.3.6 exists
    And an existing MAC_OSX groovy version 2.3.6 exists
    And the consumer for candidate groovy is making a request
    When a JSON PUT on the /candidates/default endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the default groovy version is 2.3.6 on mongodb
    And the state API received a POST /versions/tags with tag lts for groovy 2.3.6 LINUX_X64
    And the state API received a POST /versions/tags with tag lts for groovy 2.3.6 MAC_X64
    And the state API received 2 POST /versions/tags requests
    And the state API login endpoint was called 1 time

  Scenario: A java default is never propagated to the State API
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
    And an existing LINUX_64 java version 17.0.1-tem exists
    And the consumer for candidate java is making a request
    When a JSON PUT on the /candidates/default endpoint:
    """
          |{
          |   "candidate" : "java",
          |   "version" : "17.0.1-tem"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the default java version is 17.0.1-tem on mongodb
    And the state API did not receive any POST /versions/tags requests

  Scenario: A default set still succeeds when the State API tag write returns 404
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And an existing UNIVERSAL groovy version 2.3.6 exists
    And the consumer for candidate groovy is making a request
    And the state API /versions/tags endpoint returns 404
    When a JSON PUT on the /candidates/default endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the default groovy version is 2.3.6 on mongodb

  Scenario: A default set still succeeds when the State API tag write is unavailable
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And an existing UNIVERSAL groovy version 2.3.6 exists
    And the consumer for candidate groovy is making a request
    And the state API /versions/tags endpoint is unavailable
    When a JSON PUT on the /candidates/default endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the default groovy version is 2.3.6 on mongodb

  Scenario: A default set succeeds and retries once when the tag write is unauthorised
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And an existing UNIVERSAL groovy version 2.3.6 exists
    And the consumer for candidate groovy is making a request
    And the state API will return 401 on the first tags request
    When a JSON PUT on the /candidates/default endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the default groovy version is 2.3.6 on mongodb
    And the state API login endpoint was called 2 times
    And the state API received a POST /versions/tags with a Bearer token
    And the state API received 2 POST /versions/tags requests

  Scenario: A failing tag write on one platform does not suppress its siblings
    Given the existing default LINUX_64 groovy version is 2.3.5
    And an existing LINUX_64 groovy version 2.3.6 exists
    And an existing MAC_OSX groovy version 2.3.6 exists
    And the consumer for candidate groovy is making a request
    And the state API /versions/tags endpoint returns 404
    When a JSON PUT on the /candidates/default endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6"
          |}
    """
    Then the status received is 202 ACCEPTED
    And the default groovy version is 2.3.6 on mongodb
    And the state API received 2 POST /versions/tags requests
