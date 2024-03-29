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

Feature: Release version validation

  Background:
    Given the consumer for candidate java is making a request
    And the consumer has a valid auth token

  Scenario: The version is marked as a default explicitly
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
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

  Scenario: The version is not marked as a default explicitly
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
    And the URI /zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz is available for download
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "vendor" : "zulu",
          |  "version" : "8u131",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "LINUX_64",
          |  "default" : false
          |}
    """
    Then the status received is 201 CREATED
    And the default java version is 8u121-zulu on mongodb

  Scenario: The version is not marked as a default implicitly
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
    And the URI /zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz is available for download
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "vendor" : "zulu",
          |  "version" : "8u131",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "LINUX_64"
          |}
    """
    Then the status received is 201 CREATED
    And the default java version is 8u121-zulu on mongodb
