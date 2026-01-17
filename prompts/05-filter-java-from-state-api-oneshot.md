# Filter Java Releases from State API Propagation

When vendors release new Java SDK versions through the Vendor Release API, these versions should continue to be persisted to MongoDB for historical tracking and vendor management purposes. However, unlike other SDK candidates, Java versions should NOT be propagated to the State API's `/versions` endpoint. This prevents Java releases from appearing in the public-facing SDKMAN! distribution system while maintaining complete internal records.

## Requirements

- Java candidate releases MUST be persisted to MongoDB as normal
- Java candidate releases MUST NOT call the State API `/versions` endpoint
- Non-Java candidate releases MUST continue to be persisted to MongoDB AND propagated to State API
- The HTTP response for Java releases should return success (2xx) since MongoDB persistence succeeded
- Logging should clearly indicate when State API propagation is skipped for Java candidates
- Existing Java versions already in the State API should be left unchanged (no migration/cleanup)
- The filtering logic should be based on the candidate identifier being "java" (case-insensitive comparison)

## Rules

- rules/scala-rules.md
- rules/ddd-rules.md

## Domain

The core change affects the version release flow in `VersionReleaseRoutes.scala:80-89`:

```scala
// Current flow (all candidates):
for {
  _ <- upsertVersionMongodb(v.copy(version = mongoVersionString))  // MongoDB persistence
  _ <- upsertVersionStateApi(v).recoverWith {                      // State API propagation
    case ex: Exception =>
      logger.error(s"Failed to upsert version to state API: ${ex.getMessage}", ex)
      Future.unit
  }
  // ... default version handling
} yield createdResponse(...)

// New flow (with Java filtering):
for {
  _ <- upsertVersionMongodb(v.copy(version = mongoVersionString))  // MongoDB persistence (always)
  _ <- conditionalStateApiPropagation(v)                           // State API propagation (conditional)
  // ... default version handling
} yield createdResponse(...)

def conditionalStateApiPropagation(version: Version): Future[Unit] = {
  if (version.candidate.equalsIgnoreCase("java")) {
    logger.info(s"Skipping State API propagation for Java candidate: ${version.version}")
    Future.successful(())
  } else {
    upsertVersionStateApi(version).recoverWith {
      case ex: Exception =>
        logger.error(s"Failed to upsert version to state API: ${ex.getMessage}", ex)
        Future.unit
    }
  }
}
```

Key domain concepts:
- **Candidate**: An SDK type (java, kotlin, scala, gradle, etc.)
- **Version**: A specific release of a candidate with platform, URL, checksums, and vendor metadata
- **MongoDB Persistence**: Long-term storage for all releases (Java and non-Java)
- **State API Propagation**: Public distribution mechanism for non-Java releases only

## Extra Considerations

- **Performance**: The candidate check (`version.candidate.equalsIgnoreCase("java")`) is a simple string comparison with negligible performance impact
- **Case Sensitivity**: Use case-insensitive comparison to handle any variations in how "java" might be submitted (Java, JAVA, java)
- **Error Handling**: The existing error recovery for State API failures should be preserved for non-Java candidates
- **Logging**: Different log levels for different scenarios:
  - INFO when skipping Java propagation (expected behavior)
  - ERROR when State API fails for non-Java candidates (unexpected)
- **Backwards Compatibility**: Existing Java versions in State API are left untouched, so no data migration or cleanup required
- **Multi-Platform Releases**: Each platform release (Linux, macOS, Windows) of a Java version should be filtered individually

## Testing Considerations

### Test Data Files

**IMPORTANT:** Reuse existing test files in `src/test/resources/__files/`:
- Java candidate: `zulu8.21.0.1-jdk8.0.131.tar.gz` (version 8.0.131)
- Non-Java candidate (Groovy): `groovy-2.3.6.zip` (version 2.3.6)

These files are already set up for URL validation in the test suite.

### Cucumber Feature Scenarios

Create a new feature file: `filter_java_state_api_propagation.feature`

**Happy Path:**
- Release Java 8.0.131 → MongoDB updated, State API NOT called, returns 201 Created
- Release Groovy 2.3.6 → MongoDB updated, State API called, returns 201 Created

**Edge Cases:**
- Java candidate with different case variations in the "candidate" field ("JAVA", "Java", "java") → all filtered correctly
- Java version with vendor suffix (e.g., vendor="zulu") → MongoDB gets suffix appended (e.g., "8.0.131-zulu"), State API not called
- Java version set as default → MongoDB default updated, State API not called

**Verification:**
- Stub the State API `/versions` endpoint with WireMock
- Assert stub is NOT called for Java releases
- Assert stub IS called for non-Java releases
- Verify MongoDB contains all releases (Java and non-Java)

### Unit Testing

Extend `VersionReleaseRoutesSpec` (if it exists) or create integration tests to verify:
- The conditional logic correctly identifies "java" candidate
- Future composition ensures MongoDB persists before State API check
- Error recovery still works for non-Java candidates

## Implementation Notes

**Preferred Approach:**
1. Create a private helper method `shouldPropagateToStateApi(candidate: String): Boolean` in `VersionReleaseRoutes`
2. Wrap the `upsertVersionStateApi` call with this conditional check
3. Add appropriate logging for both paths (skipped vs propagated)
4. Maintain existing error recovery for non-Java propagation failures

**Alternative Approach (not recommended):**
Moving the logic into `HttpStateApiClient.upsertVersionStateApi` would violate separation of concerns. The State API client should remain agnostic to business rules about which candidates to propagate.

**Code Location:**
- Primary change: `src/main/scala/io/sdkman/vendor/release/routes/VersionReleaseRoutes.scala:80-89`
- No changes needed to: `HttpStateApiClient.scala` (stays generic)

**Formatting:**
- Run `sbt scalafmtAll` before committing

## Specification by Example

### Example 1: Java Version Release (Filtered)

**HTTP Request:**
```http
POST /versions
Content-Type: application/json
Consumer: valid-consumer-token

{
  "candidate": "java",
  "version": "8.0.131",
  "url": "http://localhost:8080/zulu8.21.0.1-jdk8.0.131.tar.gz",
  "platform": "UNIVERSAL"
}
```

**Expected Behavior:**
- MongoDB: Version saved with candidate="java", version="8.0.131"
- State API: `/versions` endpoint NOT called
- Logs: `INFO: Skipping State API propagation for Java candidate: 8.0.131`
- Response: `201 Created` with body containing candidate, version, platform

**WireMock Verification:**
```scala
verify(0, postRequestedFor(urlEqualTo("/versions")))
```

### Example 2: Non-Java Version Release (Propagated)

**HTTP Request:**
```http
POST /versions
Content-Type: application/json
Consumer: valid-consumer-token

{
  "candidate": "groovy",
  "version": "2.3.6",
  "url": "http://localhost:8080/groovy-2.3.6.zip",
  "platform": "UNIVERSAL"
}
```

**Expected Behavior:**
- MongoDB: Version saved with candidate="groovy", version="2.3.6"
- State API: POST to `/versions` with StateVersion payload
- Logs: `INFO: Upserted to <state-api-url>/versions: groovy 2.3.6 UNIVERSAL`
- Response: `201 Created`

**WireMock Verification:**
```scala
verify(1, postRequestedFor(urlEqualTo("/versions"))
  .withRequestBody(containing("\"candidate\":\"groovy\""))
  .withRequestBody(containing("\"version\":\"2.3.6\"")))
```

### Example 3: Cucumber Scenario

**NOTE:** Use existing test files from `src/test/resources/__files/`:
- Java: `zulu8.21.0.1-jdk8.0.131.tar.gz`
- Groovy: `groovy-2.3.6.zip`

```gherkin
Feature: Filter Java releases from State API propagation

  Background:
    Given the consumer has a valid auth token
    And the state API is available

  Scenario: Java version should be persisted to MongoDB but not propagated to State API
    Given the existing default UNIVERSAL java version is 8.0.130
    And the consumer for candidate java is making a request
    And the URI /zulu8.21.0.1-jdk8.0.131.tar.gz is available for download
    When a JSON POST on the /versions endpoint:
      """
      {
        "candidate": "java",
        "version": "8.0.131",
        "url": "http://localhost:8080/zulu8.21.0.1-jdk8.0.131.tar.gz",
        "platform": "UNIVERSAL"
      }
      """
    Then the status received is 201 CREATED
    And java version 8.0.131 with URL http://localhost:8080/zulu8.21.0.1-jdk8.0.131.tar.gz was published for UNIVERSAL to mongodb
    And the state API did not receive any POST requests

  Scenario: Non-Java version should be persisted to MongoDB and propagated to State API
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    When a JSON POST on the /versions endpoint:
      """
      {
        "candidate": "groovy",
        "version": "2.3.6",
        "url": "http://localhost:8080/groovy-2.3.6.zip",
        "platform": "UNIVERSAL"
      }
      """
    Then the status received is 201 CREATED
    And groovy version 2.3.6 with URL http://localhost:8080/groovy-2.3.6.zip was published for UNIVERSAL to mongodb
    And the state API received a POST request with version 2.3.6
```

## Verification

- [ ] Java version releases persist to MongoDB successfully
- [ ] Java version releases do NOT call State API `/versions` endpoint
- [ ] Non-Java version releases persist to MongoDB successfully
- [ ] Non-Java version releases DO call State API `/versions` endpoint
- [ ] Case-insensitive matching works for "java", "Java", "JAVA"
- [ ] Logs show INFO message when Java propagation is skipped
- [ ] Logs show existing State API success/error messages for non-Java candidates
- [ ] HTTP responses are 201 Created for both Java and non-Java successful releases
- [ ] Multi-platform Java releases are all filtered (LINUX_64, MAC_OSX, WINDOWS_64)
- [ ] Java versions with vendor suffixes are handled correctly (MongoDB gets suffix, State API not called)
- [ ] Setting a Java version as default works without State API propagation
- [ ] All Cucumber tests pass
- [ ] Code formatted with `sbt scalafmtAll`
- [ ] Small, incremental Git commit created after tests pass
