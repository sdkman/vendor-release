Feature: Release Version with checksums

  Background:
    Given the Consumer has a valid Auth Token

  Scenario: Release universal groovy binary with checksums
    Given the existing Default UNIVERSAL groovy Version is 2.3.6
    And the Consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "groovy",
          |  "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip",
          |  "platform" : "UNIVERSAL",
          |  "checksums" : {
          |    "MD5": "8f817c305a1bb15428b4aa29b844d75c",
          |    "SHA-1": "aa8c9101ae2badca9ca4827bd433c58cf3c255e8",
          |    "SHA-224": "2380df580f84d3d1549681a7b25b589f4371becfca62f1e0a985f5b4",
          |    "SHA-256": "01bfe9d471b7cb1f8321204e6fa05a574db3ae5b67c5bd2f17184ffd521387f1",
          |    "SHA-384": "337155d0cff16f0448de8111247927382c53a9ed25aa260301f3233f94945a3704359ef7b44f3425f03a4ad79e97269d",
          |    "SHA-512": "6b9bf0fd263b4eeb9929df2ffcb3bc765212e682e7dca08bc48ce4e4950cdde10282b029b60291c8dad0d8d1b65574979dff2c657e58ced7846b01f485ecec59"
          |  }
          |}
    """
    Then the status received is 201 CREATED
    And groovy Version 2.3.6 with URL http://localhost:8080/groovy-2.3.6.zip was published as UNIVERSAL to mongodb
    And groovy Version 2.3.6 on platform UNIVERSAL has a checksum "8f817c305a1bb15428b4aa29b844d75c" using algorithm MD5
    And groovy Version 2.3.6 on platform UNIVERSAL has a checksum "aa8c9101ae2badca9ca4827bd433c58cf3c255e8" using algorithm SHA-1
    And groovy Version 2.3.6 on platform UNIVERSAL has a checksum "2380df580f84d3d1549681a7b25b589f4371becfca62f1e0a985f5b4" using algorithm SHA-224
    And groovy Version 2.3.6 on platform UNIVERSAL has a checksum "01bfe9d471b7cb1f8321204e6fa05a574db3ae5b67c5bd2f17184ffd521387f1" using algorithm SHA-256
    And groovy Version 2.3.6 on platform UNIVERSAL has a checksum "337155d0cff16f0448de8111247927382c53a9ed25aa260301f3233f94945a3704359ef7b44f3425f03a4ad79e97269d" using algorithm SHA-384
    And groovy Version 2.3.6 on platform UNIVERSAL has a checksum "6b9bf0fd263b4eeb9929df2ffcb3bc765212e682e7dca08bc48ce4e4950cdde10282b029b60291c8dad0d8d1b65574979dff2c657e58ced7846b01f485ecec59" using algorithm SHA-512

  Scenario: Release multiple multi-platform binaries of the same Version with checksums
    Given an existing LINUX_64 java Version 8u121-zulu exists
    And the Consumer for candidate java|jmc is making a request
    And the URI /zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz is available for download
    And the URI /zulu8.21.0.1-jdk8.0.131-macosx.tar.gz is available for download
    And the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "LINUX_64",
          |  "checksums" : {
          |    "SHA-224": "2380df580f84d3d1549681a7b25b589f4371becfca62f1e0a985f5b4"
          |  }
          |}
    """
    Then the status received is 201 CREATED
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-macosx.tar.gz",
          |  "platform" : "MAC_OSX",
          |  "checksums" : {
          |    "SHA-384": "337155d0cff16f0448de8111247927382c53a9ed25aa260301f3233f94945a3704359ef7b44f3425f03a4ad79e97269d"
          |  }
          |}
    """
    Then the status received is 201 CREATED
    And java Version 8u131-zulu with URL http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz was published as LINUX_64 to mongodb
    And java Version 8u131-zulu on platform LINUX_64 has a checksum "2380df580f84d3d1549681a7b25b589f4371becfca62f1e0a985f5b4" using algorithm SHA-224
    And java Version 8u131-zulu with URL http://localhost:8080/zulu8.21.0.1-jdk8.0.131-macosx.tar.gz was published as MAC_OSX to mongodb
    And java Version 8u131-zulu on platform MAC_OSX has a checksum "337155d0cff16f0448de8111247927382c53a9ed25aa260301f3233f94945a3704359ef7b44f3425f03a4ad79e97269d" using algorithm SHA-384

  Scenario: Attempt to submit malformed JSON with invalid algorithms
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    And the Consumer for candidate java|jmc is making a request
    And the URI /zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz is available for download
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "LINUX_64",
          |  "checksums" : {
          |    "SHA-1": "aa8c9101ae2badca9ca4827bd433c58cf3c255e8",
          |    "MD4": "8f817c305a1bb15428b4aa29b844d75c",
          |    "SHA-500": "sha500-checksum"
          |  }
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message containing "Invalid algorithm(s): MD4,SHA-500" is received

  Scenario: Attempt to submit malformed JSON with invalid checksums
    Given the existing Default PLATFORM_SPECIFIC java Version is 8u121-zulu
    And the Consumer for candidate java|jmc is making a request
    And the URI /zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz is available for download
    When a JSON POST on the /release/version endpoint:
    """
          |{
          |  "candidate" : "java",
          |  "version" : "8u131-zulu",
          |  "url" : "http://localhost:8080/zulu8.21.0.1-jdk8.0.131-linux_x64.tar.gz",
          |  "platform" : "LINUX_64",
          |  "checksums" : {
          |    "MD5": "md5",
          |    "SHA-1": "",
          |    "SHA-224": "sha224",
          |    "SHA-256": "01bfe9d471b7cb1f8321204e6fa05a574db3ae5b67c5bd2f17184ffd521387f1",
          |    "SHA-384": "337155d0cff16f0448de8111247927382c53a9ed25aa260301",
          |    "SHA-512": "512"
          |  }
          |}
    """
    Then the status received is 400 BAD_REQUEST
    And the message containing "Invalid checksum for algorithm(s): MD5,SHA-1,SHA-224,SHA-384,SHA-512" is received

  Scenario: Change the checksum of an existing Candidate Version
    Given the Consumer for candidate groovy is making a request
    And the URI /groovy-x.y.z.zip is available for download
    And the UNIVERSAL candidate groovy with default version 2.3.6 already exists
    And an existing UNIVERSAL groovy Version 2.3.6 exists
    When a JSON PATCH on the /release/version endpoint:
    """
          |{
          |   "candidate" : "groovy",
          |   "version" : "2.3.6",
          |   "platform": "UNIVERSAL",
          |   "url" : "http://localhost:8080/groovy-x.y.z.zip",
          |   "checksums" : {
          |    "MD5": "8f817c305a1bb15428b4aa29b844d75c",
          |    "SHA-256": "01bfe9d471b7cb1f8321204e6fa05a574db3ae5b67c5bd2f17184ffd521387f1"
          |   }
          |}
    """
    Then the status received is 204 NO_CONTENT
    And groovy Version 2.3.6 with URL http://localhost:8080/groovy-x.y.z.zip was published as UNIVERSAL to mongodb
    And groovy Version 2.3.6 on platform UNIVERSAL has a checksum "8f817c305a1bb15428b4aa29b844d75c" using algorithm MD5
    And groovy Version 2.3.6 on platform UNIVERSAL has a checksum "01bfe9d471b7cb1f8321204e6fa05a574db3ae5b67c5bd2f17184ffd521387f1" using algorithm SHA-256
