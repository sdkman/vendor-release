# Remove Vendor Suffix Support from Version Field

The current implementation allows vendor suffixes to be embedded within the version field (e.g., "17.0.1-tem"), creating ambiguity and inconsistency between MongoDB storage and State API representation. This feature removes that capability and enforces explicit separation of version and vendor identifiers.

## Requirements

- The `version` field MUST NOT contain a vendor suffix pattern (e.g., `-tem`, `-zulu`, `-amzn`)
- If a `version` field contains a hyphen followed by text that could be a vendor suffix, the API MUST return a 400 Bad Request response
- When both `version` and `vendor` fields are provided, MongoDB MUST store the concatenated `version` value as `"version-vendor"` (e.g., "17.0.1-tem"), as well as a `vendor` of `tem`
- When both `version` and `vendor` fields are provided, the State API MUST receive them as separate fields
- The validation MUST occur before any database operations

## Rules

- rules/scala.md
- rules/ddd.md

## Domain

```scala
// Version release request
case class VersionReleaseRequest(
    val candidate: String,        // e.g., "java"
    val version: String,          // e.g., "17.0.1" (NO suffix allowed)
    val url: String,
    val platform: Option<String>,
    val vendor: Option<String>,   // e.g., "tem" (explicit vendor identifier)
    val checksums: Option<Map<String, String>>,
    val default: Option<Boolean>
)

// Storage representation
case class MongoVersion(
    val version: String           // If vendor present: "17.0.1-tem", otherwise: "17.0.1"
)

// State API representation
case class StateApiVersion(
    val version: String,          // Always: "17.0.1"
    val vendor: Option<String>    // If provided: "tem"
)
```

## Extra Considerations

- The validation should detect all common vendor suffix patterns:
  - amzn
  - albba
  - gln
  - graalce
  - graal
  - bisheng
  - open
  - jbr
  - librca
  - nik
  - mandrel
  - ms
  - oracle
  - sapmchn
  - sem
  - tem
  - kona
  - trava
  - zulu
- Consider version strings that _legitimately_ contain hyphens (e.g., "1.0.0-rc1", "2.0.0-beta-1") - **these are NOT vendor suffixes**
- The error message for invalid version format should be clear and actionable
- The `Vendor` header behavior should remain unchanged and continue to work alongside the `vendor` request field

## Testing Considerations

- Unit tests for the vendor suffix validation logic covering:
  - Valid versions without suffixes: "17.0.1", "1.0.0", "2024.1"
  - Invalid versions with vendor suffixes: "17.0.1-tem", "11.0.12-zulu"
  - Edge cases: versions with legitimate hyphens like "1.0.0-rc1", "2.0.0-SNAPSHOT"
- Cucumber acceptance tests for the release endpoint covering:
  - Happy path: version without suffix, with explicit vendor field
  - Happy path: version without suffix, without vendor field
  - Unhappy path: version with vendor suffix, expect 400 response
  - Unhappy path: version with vendor suffix AND vendor field, expect 400 response
  - Verify MongoDB storage format when vendor is present
  - Verify State API receives correct separate fields

## Implementation Notes

- Add validation logic to the `Validation` trait or create a new validator
- The validation should be applied in the `validate` method chain in `VersionReleaseRoutes.scala`
- Use a regex pattern to detect vendor suffixes: consider suffixes that are 2-10 lowercase letters after a hyphen at the end of the version string
- Distinguish between vendor suffixes and valid pre-release/build metadata patterns (semantic versioning)
- Keep the existing MongoDB concatenation logic (line 79 in VersionReleaseRoutes.scala) unchanged
- Keep the existing MongoDB persistence logic of the vendor field
- Ensure the State API continues to receive separate version and vendor fields

## Specification by Example

### Example 1: Valid request with explicit vendor
```json
POST /versions
{
  "candidate": "java",
  "version": "17.0.1",
  "url": "https://example.com/java-17.0.1-tem.zip",
  "vendor": "tem"
}

Response: 201 Created
MongoDB stores: version = "17.0.1-tem"
State API receives: version = "17.0.1", vendor = "tem"
```

### Example 2: Valid request without vendor
```json
POST /versions
{
  "candidate": "java",
  "version": "17.0.1",
  "url": "https://example.com/java-17.0.1.zip"
}

Response: 201 Created
MongoDB stores: version = "17.0.1"
State API receives: version = "17.0.1", vendor = null
```

### Example 3: Invalid request with vendor suffix in version
```json
POST /versions
{
  "candidate": "java",
  "version": "17.0.1-tem",
  "url": "https://example.com/java-17.0.1-tem.zip"
}

Response: 400 Bad Request
{
  "status": 400,
  "message": "Invalid version format: version field must not contain vendor suffix. Use the 'vendor' field instead."
}
```

### Example 4: Invalid request with both suffix and vendor field
```json
POST /versions
{
  "candidate": "java",
  "version": "17.0.1-tem",
  "url": "https://example.com/java-17.0.1-tem.zip",
  "vendor": "tem"
}

Response: 400 Bad Request
{
  "status": 400,
  "message": "Invalid version format: version field must not contain vendor suffix. Use the 'vendor' field instead."
}
```

### Example 5: Valid pre-release version (should pass)
```json
POST /versions
{
  "candidate": "kotlin",
  "version": "2.0.0-RC1",
  "url": "https://example.com/kotlin-2.0.0-RC1.zip"
}

Response: 201 Created
MongoDB stores: version = "2.0.0-RC1"
State API receives: version = "2.0.0-RC1", vendor = null
```

## Verification

- [ ] Version request field with embedded vendor suffix is rejected with 400 status
- [ ] Error message clearly explains to use the `vendor` field instead
- [ ] Version without suffix and with explicit `vendor` field is accepted
- [ ] MongoDB stores concatenated "version-vendor" when vendor is provided
- [ ] State API receives separate version and vendor fields
- [ ] Pre-release versions with patterns like (-rc1, -SNAPSHOT, -beta-1) are not rejected
- [ ] All existing Cucumber tests continue to pass
- [ ] New Cucumber test for vendor suffix validation passes
- [ ] Code is formatted with `sbt scalafmtAll`
