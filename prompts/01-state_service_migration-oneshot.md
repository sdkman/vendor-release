# SDKMAN State Service Contract Compatibility

During the migration from MongoDB persistence to the new PostgreSQL-based sdkman-state service, the vendor-release service needs to maintain dual persistence while ensuring contract compatibility between services. The vendor-release service currently persists to MongoDB and makes HTTP calls to sdkman-state, but critical data contract mismatches exist that prevent successful integration.

## Requirements

- Fix platform type mapping between vendor-release (String) and sdkman-state (Platform enum)
- Align vendor field handling between services (Optional[String] vs String with default)
- Ensure checksum field structure compatibility (Map[String,String] vs individual Option[String] fields)
- Maintain backwards compatibility with existing MongoDB persistence during migration
- Verify HTTP client authentication and endpoint contracts match server expectations
- Ensure version creation flow works end-to-end with both persistence layers

## Rules

- rules/ddd-rules.md
- rules/scala-rules.md

## Domain

```kotlin
// Current sdkman-state domain model
data class Version(
    val candidate: String,
    val version: String,
    val vendor: String,              // Required field, no Optional
    val platform: Platform,         // Enum type
    val url: String,
    val visible: Boolean,
    val md5sum: Option<String> = None,
    val sha256sum: Option<String> = None,
    val sha512sum: Option<String> = None,
)

enum class Platform {
    LINUX_X32, LINUX_X64, LINUX_ARM32HF, LINUX_ARM32SF, LINUX_ARM64,
    MAC_X64, MAC_ARM64, WINDOWS_X64, UNIVERSAL
}
```

```scala
// vendor-release model used against the remote API
case class StateVersion(
    candidate: String,
    version: String,
    vendor: String,
    url: String,
    platform: String = "UNIVERSAL", // Default string
    visible: Boolean = true,
    md5sum: Option[String] = None,
    sha256sum: Option[String] = None,
    sha512sum: Option[String] = None
)

// vendor-release core domain and persistent model for Version
case class Version(
    candidate: String,
    version: String,
    platform: String,
    url: String,
    vendor: Option[String] = None,
    visible: Option[Boolean] = Some(true),
    checksums: Option[Map[String, String]] = None
)

```

## Testing Considerations

- Acceptance test covering full version release workflow with both persistence layers (happy path)
- Integration tests verifying `HttpStateApiClient` matches server API expectations against WireMock
- Unit tests for data transformation between domain models (Version -> StateVersion)
- Error scenario testing for state service unavailability
- Platform enum mapping edge cases and invalid values
- Checksum field transformation accuracy

## Implementation Notes

- Use existing `HttpStateApiClient` as the integration point
- Maintain existing MongoDB persistence **unchanged** during migration
- Add comprehensive logging for dual-write operations
- Follow existing Akka HTTP patterns for error handling
- Preserve current JSON serialization approach with spray-json
- Use defensive programming for Optional/nullable field handling

## Specification by Example

### Current POST to sdkman-state (broken contract):
```json
{
  "candidate": "java",
  "version": "17.0.1-tem",
  "vendor": "DEFAULT",
  "url": "https://example.com/java-17.zip",
  "platform": "UNIVERSAL",    // String, should match sdkman-state Platform enum values
  "visible": true,
  "md5sum": null,
  "sha256sum": null,
  "sha512sum": null
}
```

### Expected POST format for sdkman-state:
```json
{
  "candidate": "java",
  "version": "17.0.1", 
  "vendor": "temurin",        // Never null/empty
  "url": "https://example.com/java-17.zip",
  "platform": "LINUX_X64",   // Maps to Platform enum
  "visible": true,
  "md5sum": "abc123...",      // Extracted from checksums map if present
  "sha256sum": "def456...",   // Extracted from checksums map if present
  "sha512sum": null
}
```

### Version creation flow:
```gherkin
  Given a POST /versions request to vendor-release
  And the candidate is `java`
  And the version is `17.0.1-tem`
  When the request contains checksums: {"MD5": "abc123", "SHA-256": "def456"}
  And the platform is `MAC_OSX` (legacy platform identifier)
  Then the MongoDB version is saved with original structure
  And the StateVersion is transformed with:
    | candidate| "java"                   |
    | version  | "17.0.1"                 |
    | platform | MAC_X64 (state-api enum) |
    | vendor   | "temurin"                |
    | md5sum   | "abc123"                 |
    | sha256sum| "def456"                 |
And POST to sdkman-state /versions succeeds with 204 No Content
```

## Extra Considerations

- Platform enum mapping must enforce case sensitivity and default fallbacks
- Vendor field transformation from `Option[String]` to correct String
- `null` fields should be omitted from the POST payload
- Checksum extraction from Map[String,String] to individual `Option` fields
- HTTP client error handling for state service integration failures
- Version-vendor concatenation should not be propagated to the State API
- Version and Vendor should be propagated in their own discrete fields

## Verification

- [ ] Platform string values correctly map to sdkman-state Platform enum values
- [ ] Vendor field never sends null/empty to state service (use "DEFAULT" fallback)
- [ ] Checksum map values correctly extract to individual md5sum/sha256sum/sha512sum fields
- [ ] HTTP POST to sdkman-state /versions returns 204 No Content on success
- [ ] MongoDB persistence continues working unchanged during dual-write
- [ ] Acceptance test covers full version release with both persistence layers
- [ ] Integration test covers all permutations possible against mock State API
- [ ] Error handling gracefully manages state service failures
- [ ] Version-vendor concatenation translates to individual fields