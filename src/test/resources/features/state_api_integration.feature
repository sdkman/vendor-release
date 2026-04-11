#
#  Copyright 2025 SDKMAN!
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

Feature: State API Dual Write

  Background:
    Given the consumer has a valid auth token
    And the state API is available

  Scenario: Release non-java version with dual write to verify State API integration
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip",
          |  "platform" : "UNIVERSAL"
          |}
    """
    Then the status received is 201 CREATED
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-2.3.6.zip was published for UNIVERSAL to mongodb
    And the state API received a POST request with a Bearer token
    And the state API received a POST request with version 2.3.6

  Scenario: JWT token is cached and reused across multiple releases
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip",
          |  "platform" : "UNIVERSAL"
          |}
    """
    Then the status received is 201 CREATED
    And a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip",
          |  "platform" : "UNIVERSAL"
          |}
    """
    Then the status received is 201 CREATED
    And the state API login endpoint was called 1 time

  Scenario: Version release succeeds when JWT re-authentication is needed
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    And the state API will return 401 on the first version request
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip",
          |  "platform" : "UNIVERSAL"
          |}
    """
    Then the status received is 201 CREATED
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-2.3.6.zip was published for UNIVERSAL to mongodb
    And the state API login endpoint was called 2 times
    And the state API received a POST request with a Bearer token

  Scenario: Java version should NOT be propagated to State API (filtered)
    Given the existing default MAC_OSX java version is 17.0.0
    And the consumer for candidate java|jmc is making a request
    And the URI /java-17.0.1.zip is available for download
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "17.0.1",
          |  "url" : "http://localhost:8080/java-17.0.1.zip",
          |  "vendor" : "tem",
          |  "platform" : "MAC_OSX",
          |  "checksums" : {
          |    "MD5": "8f817c305a1bb15428b4aa29b844d75c",
          |    "SHA-256": "01bfe9d471b7cb1f8321204e6fa05a574db3ae5b67c5bd2f17184ffd521387f1"
          |  }
          |}
    """
    Then the status received is 201 CREATED
    And java version 17.0.1-tem with URL http://localhost:8080/java-17.0.1.zip was published for MAC_OSX to mongodb
    And the state API did not receive any POST requests

  Scenario: Java version should NOT be propagated to State API for different platforms
    Given the existing default PLATFORM_SPECIFIC java version is 17.0.1-tem
    And the consumer for candidate java|jmc is making a request
    And the URI /java-17.0.1.zip is available for download
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "17.0.1",
          |  "url" : "http://localhost:8080/java-17.0.1.zip",
          |  "platform" : "LINUX_64"
          |}
    """
    Then the status received is 201 CREATED
    And the state API did not receive any POST requests

  Scenario: Java version should NOT be propagated to State API without vendor
    Given the existing default PLATFORM_SPECIFIC java version is 17.0.1-tem
    And the consumer for candidate java|jmc is making a request
    And the URI /java-17.0.1.zip is available for download
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "17.0.1",
          |  "url" : "http://localhost:8080/java-17.0.1.zip",
          |  "platform" : "MAC_OSX"
          |}
    """
    Then the status received is 201 CREATED
    And the state API did not receive any POST requests

  Scenario: Release version still succeeds when State API is unavailable
    Given the existing default UNIVERSAL java version is 17.0.1
    And the consumer for candidate java|jmc is making a request
    And the URI /java-17.0.1.zip is available for download
    And the state API is unavailable
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "17.0.1",
          |  "url" : "http://localhost:8080/java-17.0.1.zip",
          |  "platform" : "UNIVERSAL"
          |}
    """
    Then the status received is 201 CREATED
    And java version 17.0.1 with URL http://localhost:8080/java-17.0.1.zip was published for UNIVERSAL to mongodb

  Scenario: Version release succeeds when login credentials are invalid
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    And the state API login endpoint returns 401
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip",
          |  "platform" : "UNIVERSAL"
          |}
    """
    Then the status received is 201 CREATED
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-2.3.6.zip was published for UNIVERSAL to mongodb
    And the state API did not receive any POST requests to the versions endpoint

  Scenario: Version release succeeds when login is rate limited
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    And the state API login endpoint returns 429
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip",
          |  "platform" : "UNIVERSAL"
          |}
    """
    Then the status received is 201 CREATED
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-2.3.6.zip was published for UNIVERSAL to mongodb
    And the state API did not receive any POST requests to the versions endpoint
