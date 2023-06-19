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

Feature: Release version vendor selection

  Background:
    Given the URI /groovy-2.3.6.zip is available for download

  Scenario: A vendor field is passed from upstream
    Given the consumer for candidate groovy is making a request
    And the consumer has a valid auth token
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "platform": "UNIVERSAL",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip",
          |  "vendor": "oci"
          |}
    """
    Then the status received is 201 "CREATED"
    And the UNIVERSAL groovy version 2.3.6-oci has a vendor of 'oci'

  Scenario: A vendor header is passed from upstream
    Given the consumer for candidate groovy is making a request
    And the consumer has a valid auth token
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    And Vendor header 'oci' is passed with the request
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "platform": "UNIVERSAL",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 201 "CREATED"
    And the UNIVERSAL groovy version 2.3.6-oci has a vendor of 'oci'

  Scenario: Both a vendor header and field are passed from upstream
    Given the consumer for candidate groovy is making a request
    And the consumer has a valid auth token
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    And Vendor header 'apache' is passed with the request
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "platform": "UNIVERSAL",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip",
          |  "vendor": "oci"
          |}
    """
    Then the status received is 201 "CREATED"
    And the UNIVERSAL groovy version 2.3.6-apache has a vendor of 'apache'

  Scenario: No vendor header or field is passed from upstream
    Given the consumer for candidate groovy is making a request
    And the consumer has a valid auth token
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    When a JSON POST on the /versions endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "platform": "UNIVERSAL",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 201 "CREATED"
    And the UNIVERSAL groovy version 2.3.6 has no vendor
