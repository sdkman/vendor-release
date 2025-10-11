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

Feature: Release version vendor suffix validation

  Scenario: Version without suffix and with explicit vendor field is accepted
    Given the consumer for candidate java is making a request
    And the consumer has a valid auth token
    And the URI /java-17.0.1.zip is available for download
    And the state API is available
    And the existing default UNIVERSAL java version is 11.0.12
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "17.0.1",
          |  "url" : "http://localhost:8080/java-17.0.1.zip",
          |  "vendor" : "tem"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: Version without suffix and without vendor field is accepted
    Given the consumer for candidate java is making a request
    And the consumer has a valid auth token
    And the URI /java-17.0.1.zip is available for download
    And the state API is available
    And the existing default UNIVERSAL java version is 11.0.12
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "17.0.1",
          |  "url" : "http://localhost:8080/java-17.0.1.zip"
          |}
    """
    Then the status received is 201 CREATED

  Scenario Outline: Version with vendor suffix is rejected
    Given the consumer for candidate java is making a request
    And the consumer has a valid auth token
    And the URI /java-17.0.1.zip is available for download
    And the state API is available
    And the existing default UNIVERSAL java version is 11.0.12
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "<version>",
          |  "url" : "http://localhost:8080/java-17.0.1.zip"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message containing "Invalid version format: version field must not contain vendor suffix. Use the 'vendor' field instead." is received

    Examples:
      | version        |
      | 17.0.1-amzn    |
      | 17.0.1-albba   |
      | 17.0.1-gln     |
      | 17.0.1-graalce |
      | 17.0.1-graal   |
      | 17.0.1-bisheng |
      | 17.0.1-open    |
      | 17.0.1-jbr     |
      | 17.0.1-librca  |
      | 17.0.1-nik     |
      | 17.0.1-mandrel |
      | 17.0.1-ms      |
      | 17.0.1-oracle  |
      | 17.0.1-sapmchn |
      | 17.0.1-sem     |
      | 17.0.1-tem     |
      | 17.0.1-kona    |
      | 17.0.1-trava   |
      | 17.0.1-zulu    |

  Scenario: Version with vendor suffix AND vendor field is rejected
    Given the consumer for candidate java is making a request
    And the consumer has a valid auth token
    And the URI /java-17.0.1.zip is available for download
    And the state API is available
    And the existing default UNIVERSAL java version is 11.0.12
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "17.0.1-tem",
          |  "url" : "http://localhost:8080/java-17.0.1.zip",
          |  "vendor" : "tem"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message containing "Invalid version format: version field must not contain vendor suffix. Use the 'vendor' field instead." is received

  Scenario Outline: Version with pre-release suffix is accepted
    Given the consumer for candidate groovy is making a request
    And the consumer has a valid auth token
    And the URI /groovy-x.y.z.zip is available for download
    And the state API is available
    And the existing default UNIVERSAL groovy version is 2.3.6
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "<version>",
          |  "url" : "http://localhost:8080/groovy-x.y.z.zip"
          |}
    """
    Then the status received is 201 CREATED

    Examples:
      | version          |
      | 3.0.0-RC1        |
      | 3.0.0-SNAPSHOT   |
      | 3.0.0-beta-1     |
      | 3.0.0-alpha-1    |
      | 3.0.0-M1         |
      | 3.0.0-PREVIEW    |
