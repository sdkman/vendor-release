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

Feature: Dual-write default version as lts tag on POST /versions

  Path 2 of the default-version lts-tag dual write: releasing a non-java version with
  "default": true folds tags:["lts"] into the existing State API version dual-write; a
  non-default release omits the tags field entirely (never an empty list). Java is skipped
  on this path exactly as the existing version dual-write already skips it.

  Background:
    Given the consumer has a valid auth token
    And the state API is available

  Scenario: Releasing a non-java version as default includes the lts tag in the dual write
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip",
          |  "platform" : "UNIVERSAL",
          |  "default" : true
          |}
    """
    Then the status received is 201 CREATED
    And the default groovy version is 2.3.6 on mongodb
    And the state API received a POST request with a Bearer token
    And the state API received a POST /versions payload containing tags ["lts"]

  Scenario: Releasing a non-java version without default sends no tags field
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
    And the state API received a POST /versions payload with no tags field

  Scenario: Releasing a java version as default is never propagated to the State API
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
    And the consumer for candidate java is making a request
    And the URI /zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz is available for download
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "vendor" : "zulu",
          |  "version" : "8u131",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "LINUX_64",
          |  "default" : true
          |}
    """
    Then the status received is 201 CREATED
    And the default java version is 8u131-zulu on mongodb
    And the state API did not receive any POST requests
