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

Feature: Release platform validation

  Background:
    Given the consumer for candidate java is making a request
    And the consumer has a valid auth token
    And the URI /zulu8.21.0.1-jdk8.0.131.tar.gz is available for download

  Scenario: The Linux 64 bit platform is valid
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131.tar.gz",
          |  "platform" : "LINUX_64"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: The Linux 32 bit platform is valid
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131.tar.gz",
          |  "platform" : "LINUX_32"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: The Linux ARM32 bit soft float platform is valid
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131.tar.gz",
          |  "platform" : "LINUX_ARM32SF"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: The Linux ARM32 bit hard float platform is valid
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131.tar.gz",
          |  "platform" : "LINUX_ARM32HF"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: The Linux ARM64 bit platform is valid
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131.tar.gz",
          |  "platform" : "LINUX_ARM64"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: The Mac OSX X64 platform is valid
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131.tar.gz",
          |  "platform" : "MAC_OSX"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: The Mac OSX ARM64 platform is valid
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131.tar.gz",
          |  "platform" : "MAC_ARM64"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: The Cygwin platform is valid
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131.tar.gz",
          |  "platform" : "WINDOWS_64"
          |}
    """
    Then the status received is 201 CREATED

  Scenario: An invalid platform is rejected
    Given the existing default PLATFORM_SPECIFIC java version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131.tar.gz",
          |  "platform" : "SOLARIS"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message containing "Invalid platform: SOLARIS" is received
