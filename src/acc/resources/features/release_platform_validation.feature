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

Feature: Release Platform Validation

  Background:
    Given the Consumer java is making a request
    And the Consumer has a valid Auth Token
    And the URI /zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz is available for download

  Scenario: The Linux 64 bit Platform is valid
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
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

  Scenario: The Linux 32 bit Platform is valid
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "LINUX_32"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: The Linux ARM32 bit Platform is valid
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "LINUX_ARM32"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: The Linux ARM64 bit Platform is valid
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "LINUX_ARM64"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: The Mac OSX X64 Platform is valid
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "MAC_OSX"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: The Mac OSX ARM64 Platform is valid
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "MAC_ARM64"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: The Cygwin Platform is valid
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "WINDOWS_64"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: An invalid Platform is rejected
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "SOLARIS"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message containing "Invalid platform: SOLARIS" is received
