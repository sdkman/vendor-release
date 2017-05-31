Feature: Release Platform Validation

  Scenario: The Linux 64 bit Platform is valid
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://cdn.azul.com/zulu/bin/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "LINUX_64"
          |}
    """
    Then the status received is 201 CREATED


  Scenario: The Mac OSX Platform is valid
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://cdn.azul.com/zulu/bin/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "MAC_OSX"
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
          |  "url" : "http://cdn.azul.com/zulu/bin/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
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
          |  "url" : "http://cdn.azul.com/zulu/bin/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "SOLARIS"
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message "Invalid platform: SOLARIS" is received
