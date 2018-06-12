#
#  Copyright 2018 Marco Vermeulen
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

Feature: Release URL Validation

  Background:
    Given the Consumer java is making a request
    And the Consumer has a valid Auth Token

  Scenario: The URI is a valid resolving resource
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    And the URI /zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz is available for download
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "LINUX_64"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: The URI is NOT a valid resolving resource
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    And the URI /zulu8.21.0.1-jdk8.0.999-linux_x64.tar.gz is not available for download
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.999-linux_x64.tar.gz",
          |  "platform" : "LINUX_64"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message "URL cannot be resolved: http://localhost:8080/zulu8.21.0.1-jdk8.0.999-linux_x64.tar.gz" is received