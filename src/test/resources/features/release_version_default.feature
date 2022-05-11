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

Feature: Release Version Validation

  Background:
    Given the Consumer for candidate java is making a request
    And the Consumer has a valid Auth Token

  Scenario: The Version is marked as a default
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    And the URI /zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz is available for download
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "vendor" : "open",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "LINUX_64",
          |  "default" : false
          |}
    """
    Then the status received is 201 CREATED
