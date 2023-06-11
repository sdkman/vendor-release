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

Feature: Release Multi-Platform Version

  Background:
    Given the Consumer for candidate java|jmc is making a request
    And the Consumer has a valid Auth Token
    And the URI /zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz is available for download
    And the URI /zulu8.21.0.1-jdk8.0.131-macosx.tar.gz is available for download

  Scenario: Release a single multi-platform binary version
    Given an existing LINUX_64 java Version 8u121-zulu exists
    And the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
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
    And java Version 8u131-zulu with URL http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz was published as LINUX_64 to mongodb
    And java Version 8u131-zulu with URL http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz was published as LINUX_64 to postgres
    And the message "Released: java 8u131-zulu for LINUX_64" is received

  Scenario: Release multiple multi-platform binaries of the same version
    Given an existing LINUX_64 java Version 8u121-zulu exists
    And the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
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
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-macosx.tar.gz",
          |  "platform" : "MAC_OSX"
          |}
    """
    Then the status received is 201 CREATED
    And java Version 8u131-zulu with URL http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz was published as LINUX_64 to mongodb
    And java Version 8u131-zulu with URL http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz was published as LINUX_64 to postgres
    And java Version 8u131-zulu with URL http://localhost:8080/zulu8.21.0.1-jdk8.0.131-macosx.tar.gz was published as MAC_OSX to mongodb
    And java Version 8u131-zulu with URL http://localhost:8080/zulu8.21.0.1-jdk8.0.131-macosx.tar.gz was published as MAC_OSX to postgres
