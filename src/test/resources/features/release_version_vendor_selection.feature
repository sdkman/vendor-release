Feature: Release Version Vendor selection

  Background:
    Given the URI /groovy-2.3.6.zip is available for download

  Scenario: A Vendor field is passed from upstream
    Given the Consumer for candidate groovy is making a request
    And the Consumer has a valid Auth Token
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    When a JSON POST on the /release/version endpoint:
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
    And the UNIVERSAL groovy Version 2.3.6 has a vendor of 'oci'

  Scenario: A Vendor header is passed from upstream
    Given the Consumer for candidate groovy is making a request
    And the Consumer has a valid Auth Token
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    And Vendor header 'oci' is passed with the request
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "platform": "UNIVERSAL",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 201 "CREATED"
    And the UNIVERSAL groovy Version 2.3.6 has a vendor of 'oci'

  Scenario: Both a Vendor header and field are passed from upstream
    Given the Consumer for candidate groovy is making a request
    And the Consumer has a valid Auth Token
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    And Vendor header 'apache' is passed with the request
    When a JSON POST on the /release/version endpoint:
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
    And the UNIVERSAL groovy Version 2.3.6 has a vendor of 'apache'

  Scenario: No Vendor header or field is passed from upstream
    Given the Consumer for candidate groovy is making a request
    And the Consumer has a valid Auth Token
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "platform": "UNIVERSAL",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip"
          |}
    """
    Then the status received is 201 "CREATED"
    And the UNIVERSAL groovy Version 2.3.6 has no vendor
