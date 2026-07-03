# Dual-Write Default Version as `lts` Tag to the State API

vendor-release already dual-writes released versions from MongoDB into the Postgres-backed **State API** (`upsertVersionStateApi`). What it does **not** yet propagate is the notion of a **default version**. In Mongo the default is a single field on the candidate document; in the State API the equivalent concept is a **tag** — specifically `lts` — scoped to `(candidate, distribution, platform)`.

This feature closes that gap. Whenever vendor-release sets a candidate's default version in Mongo, it must **also** reflect that default into the State API by asserting the `lts` tag on the corresponding version — a best-effort dual write that never blocks the Mongo path. Java is deliberately excluded: the DISCO pipeline writes java versions **and** their tags to the State API directly, bypassing vendor-release, exactly as the existing version dual-write already skips java. Every *other* candidate depends on vendor-release for this, so `lts` is effectively vendor-release's "this is the default version" marker in the State API.

The overwhelming majority of these non-java candidates are **UNIVERSAL** — a single, platform-agnostic version row (`UNIVERSAL` → State `UNIVERSAL`) — so in the common case the per-platform propagation described below collapses to a **single** tag write. Genuinely platform-specific non-java candidates (distinct `LINUX_64`, `MAC_OSX`, `WINDOWS_64`, … rows) exist but are the exception, not the rule. The design handles both uniformly; the platform-specific references throughout this spec should be read as the general case of which UNIVERSAL is the dominant, single-row instance.

Because `lts` is the **only** tag any non-java candidate ever carries — `latest`, `26`, and every other tag are used exclusively on java — this dual write is also the *only* writer of tags for non-java candidates in the State API.

There are two places vendor-release sets the Mongo default, and both must propagate the tag:

- `PUT /candidates/default` — the dedicated default endpoint (`CandidateDefaultRoutes`).
- `POST /versions` with `"default": true` — release-and-default in one shot (`VersionReleaseRoutes`).

## Requirements

- When `PUT /candidates/default` succeeds for a **non-java** candidate, the `lts` tag is asserted on **every** platform row of `(candidate, version)` in the State API via the append-only `POST /versions/tags` endpoint. For the common **UNIVERSAL** candidate this is the single `UNIVERSAL` row (one tag write); for a genuinely platform-specific candidate it is one write per platform row.
- When `POST /versions` carries `"default": true` for a **non-java** candidate, the dual-written State version payload includes `tags: ["lts"]`; when the release is not default, the `tags` field is **omitted** entirely.
- Java candidates are never propagated to the State API on either path (unchanged skip).
- The Mongo write path is unchanged and authoritative: the vendor still receives `202 ACCEPTED` (`PUT /candidates/default`) or `201 CREATED` (`POST /versions`) whenever the Mongo write succeeds, regardless of any State API tag outcome.
- State API tag failures (`404`, `401`, `5xx`, connection errors) are recovered and logged per-platform; one platform's failure does not stop the others and does not fail the request.
- An expired cached JWT self-heals: a `401` from a tag write triggers re-authentication and a single retry, reusing the existing token machinery.
- The `lts` tag string is a fixed constant; exactly one tag is asserted per version/platform.

## Rules

- rules/scala-rules.md
- rules/ddd-rules.md

## Domain

The default-version concept maps across the two datastores as follows. Mongo owns a candidate-wide `default` version string; the State API owns a per-`(candidate, distribution, platform)` `lts` tag pointer.

```scala
// Existing — the coordinates vendor-release already knows at each write site.
case class Version(
  candidate: String,
  version:   String,          // Mongo stores "version-vendor" when a vendor is present (java only)
  platform:  String,          // vendor-release platform id, e.g. "UNIVERSAL", "LINUX_64"
  url:       String,
  vendor:    Option[String],  // None for non-java candidates
  visible:   Option[Boolean],
  checksums: Option[Map[String, String]]
)

// State API tag-assignment coordinate (POST /versions/tags on sdkman-state).
// distribution/platform use State enum names; mapped via DistributionMapper / PlatformMapper.
case class StateTagAssignment(
  candidate:    String,
  version:      String,
  distribution: Option[String],   // None for non-java
  platform:     String,           // State platform enum, e.g. "UNIVERSAL", "LINUX_X64"
  tag:          String            // always "lts"
)

// Path 2 only — the existing State version DTO gains an optional tags field.
// Some(List("lts")) on a default release; None otherwise (never Some(Nil) — that would wipe).
case class StateVersion(
  candidate:    String,
  version:      String,
  distribution: Option[String],
  url:          String,
  platform:     String = "UNIVERSAL",
  visible:      Boolean = true,
  md5sum:       Option[String] = None,
  sha256sum:    Option[String] = None,
  sha512sum:    Option[String] = None,
  tags:         Option[List[String]] = None   // NEW
)

val LtsTag = "lts"
```

**Path 1 — `PUT /candidates/default`.** After the existing `updateDefaultVersion(candidate, version)` Mongo write, enumerate all Mongo version documents for `(candidate, version)` (`findAllVersionsByCandidateVersion`). For each, map platform via `PlatformMapper`, derive distribution via `DistributionMapper` from the doc's vendor (`None` for non-java), and assert `lts` through the State API's append-only `POST /versions/tags`. Append semantics preserve the version's other tags, and the endpoint's built-in move behaviour relocates `lts` off the prior default — so there is no need to look up or clear the old default.

**Path 2 — `POST /versions` with `"default": true`.** Fold the tag into the version dual-write that already happens (`upsertVersionStateApi`): set `tags = Some(List("lts"))` when `default` is true, otherwise leave it `None`. The State API's `POST /versions` applies *replace* semantics, which correctly moves `lts` off the prior default. Since a non-java version's tag set is only ever `["lts"]` (or empty), replacing it is exactly the desired outcome — there is nothing else to preserve.

## Extra Considerations

- **Java skip on both paths.** The `candidate.equalsIgnoreCase("java")` guard used by `conditionalStateApiPropagation` applies equally to the new tag write on `PUT /candidates/default`. Java coordinates (vendor + platform-specific version strings) are never sent to the State API from vendor-release.
- **`None` vs `Some(Nil)` for tags.** On the State side, an **absent** `tags` field leaves existing tags untouched, whereas `tags: []` **replaces** (clears) them. Path 2 must therefore send `None` on non-default releases — never an empty list.
- **Replace semantics are safe for non-java (not a wipe hazard).** `POST /versions` replaces the tag set, but a non-java candidate only ever carries the single `lts` tag — `latest`, `26`, and all other tags are used exclusively on java. The system does not *enforce* this, but it is how it functions in practice, so for `candidate != "java"` it is safe to overwrite that version's — and that candidate's — entire tag set. There are no sibling tags to lose. (This is also why Path 1's append-only mechanism and Path 2's replace mechanism are equivalent in effect for non-java: with only `lts` in play, append and replace coincide.)
- **Cross-path platform-scope asymmetry (accepted, documented).** Path 1 tags **all** platform rows of the version; Path 2 tags only the **single** platform being posted (`POST /versions` is one-platform-per-call). These coincide for **UNIVERSAL** non-java candidates — the overwhelmingly common case, where "all platform rows" *is* the one UNIVERSAL row — and diverge only for a genuinely multi-platform non-java candidate released with `default:true` on some-but-not-all platform POSTs. No reconciliation is attempted.
- **Version-string mapping.** For non-java candidates there is no vendor, so the Mongo-stored version equals the clean version and maps directly onto the State coordinate. The `version-vendor` suffix only ever arises for java, which is skipped.
- **State-vs-Mongo platform divergence.** Path 1 drives platform enumeration off the Mongo docs it already holds. If a platform exists in Mongo but its version row was never propagated to State, the tag write returns `404` — swallowed by the best-effort recovery, consistent with the drift-tolerant contract.
- **Ordering.** Mongo write first, State tag write(s) second. The State outcome never rolls back or blocks the Mongo result.

## Testing Considerations

Follow the existing Cucumber + WireMock acceptance style (`state_api_integration.feature`, `default_version.feature`, `release_version_default.feature`), stubbing the State API endpoints. Cover:

- **Path 1 happy path:** `PUT /candidates/default` for a non-java candidate issues `POST /versions/tags` with `tag=lts` for each platform row, carries a Bearer token, and returns `202`.
- **Path 1 multi-platform:** a version present on multiple platforms produces one tag call per platform.
- **Path 2 default release:** `POST /versions` with `default:true` sends a State version payload containing `tags:["lts"]`.
- **Path 2 non-default release:** `POST /versions` without default sends **no** `tags` field.
- **Java skip:** neither path issues any State API request for `candidate=java`.
- **Best-effort failures:** State `/versions/tags` returning `404`, `401` (then success on retry after re-auth), and `5xx`/unavailable each still yield a `202`/`201` and leave Mongo correct; a failure on one platform does not suppress the others.
- **Token reuse:** the login endpoint is called once across multiple tag writes; a `401` triggers exactly one re-auth + retry.
- Quality gates: `sbt test` (Cucumber suite) green; scalafmt clean.

## Implementation Notes

- **New client method** on `HttpStateApiClient`, e.g. `assignTagStateApi(candidate, version, distribution, platform, tag): Future[Unit]`, that `POST`s JSON to `\$stateApiUrl/versions/tags` with `Authorization: Bearer`. Reuse `getToken()` / `login()` and replicate the existing `401 → re-authenticate → retry-once` branch from `upsertVersionStateApi`. A success is `204 No Content`; a `404`/other non-2xx logs and completes best-effort (`recoverWith`), mirroring `conditionalStateApiPropagation`.
- **`StateVersion` DTO** gains `tags: Option[List[String]] = None`; update the spray-json format (`jsonFormat9` → `jsonFormat10`). `upsertVersionStateApi` sets `tags` from the release's default flag.
- **`CandidateDefaultRoutes`** — after the successful `updateDefaultVersion`, fold in the per-platform tag propagation (guarded by the java skip), composed into the existing `for`-comprehension without changing the `202 acceptedResponse` result. Keep the domain-service coordination free of HTTP concerns per hexagonal boundaries; the State API call lives behind the `HttpStateApiClient` port trait already mixed into the route.
- **`VersionReleaseRoutes`** — the default-tag decision rides along with the existing `req.default` handling; no new branch in the response flow, only the enriched `StateVersion` payload.
- Reuse `PlatformMapper.mapToStatePlatform` and `DistributionMapper.mapToStateDistribution` for coordinate translation; do not duplicate the mapping tables.
- Follow the functional-Scala conventions in `rules/scala-rules.md`: `Option` over null, `Future` composition over blocking, no exceptions leaking past the best-effort boundary.

## Specification by Example

```gherkin
Feature: Dual-write default version as lts tag to the State API

  Background:
    Given the consumer has a valid auth token
    And the state API is available

  Scenario: Setting a non-java default asserts the lts tag on every platform row
    Given an existing UNIVERSAL groovy version 2.3.6 exists
    And the consumer for candidate groovy is making a request
    When a JSON PUT on the /candidates/default endpoint:
    """
          |{ "candidate": "groovy", "version": "2.3.6" }
    """
    Then the status received is 202 ACCEPTED
    And the default groovy version is 2.3.6 on mongodb
    And the state API received a POST /versions/tags with tag lts for groovy 2.3.6 UNIVERSAL
    And the state API received a POST request with a Bearer token

  Scenario: Releasing a non-java version as default includes the lts tag in the dual write
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    When a JSON POST on the /versions endpoint:
    """
          |{ "candidate": "groovy", "version": "2.3.6",
          |  "url": "http://localhost:8080/groovy-2.3.6.zip",
          |  "platform": "UNIVERSAL", "default": true }
    """
    Then the status received is 201 CREATED
    And the state API received a POST /versions payload containing tags ["lts"]

  Scenario: Releasing a non-java version without default sends no tags field
    Given the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    When a JSON POST on the /versions endpoint:
    """
          |{ "candidate": "groovy", "version": "2.3.6",
          |  "url": "http://localhost:8080/groovy-2.3.6.zip",
          |  "platform": "UNIVERSAL" }
    """
    Then the status received is 201 CREATED
    And the state API received a POST /versions payload with no tags field

  Scenario: Java default is never propagated to the State API
    Given an existing PLATFORM_SPECIFIC java version 17.0.1-tem exists
    And the consumer for candidate java is making a request
    When a JSON PUT on the /candidates/default endpoint:
    """
          |{ "candidate": "java", "version": "17.0.1-tem" }
    """
    Then the status received is 202 ACCEPTED
    And the default java version is 17.0.1-tem on mongodb
    And the state API did not receive any POST requests

  Scenario: A default set still succeeds when the State API tag write fails
    Given an existing UNIVERSAL groovy version 2.3.6 exists
    And the consumer for candidate groovy is making a request
    And the state API /versions/tags endpoint returns 404
    When a JSON PUT on the /candidates/default endpoint:
    """
          |{ "candidate": "groovy", "version": "2.3.6" }
    """
    Then the status received is 202 ACCEPTED
    And the default groovy version is 2.3.6 on mongodb

  Scenario: A default set succeeds and retries once when the tag write is unauthorised
    Given an existing UNIVERSAL groovy version 2.3.6 exists
    And the consumer for candidate groovy is making a request
    And the state API will return 401 on the first tags request
    When a JSON PUT on the /candidates/default endpoint:
    """
          |{ "candidate": "groovy", "version": "2.3.6" }
    """
    Then the status received is 202 ACCEPTED
    And the state API login endpoint was called 2 times
    And the state API received a POST /versions/tags with a Bearer token
```

## Verification

- [ ] `PUT /candidates/default` for a non-java candidate issues `POST /versions/tags` (`tag=lts`) for every Mongo platform row of `(candidate, version)`
- [ ] `POST /versions` with `default:true` (non-java) dual-writes a State payload containing `tags:["lts"]`
- [ ] `POST /versions` without default sends no `tags` field (never `[]`)
- [ ] Neither path issues any State API request when `candidate=java`
- [ ] Mongo write remains authoritative; vendor receives `202`/`201` whenever Mongo succeeds
- [ ] State API tag failures (`404`/`401`/`5xx`/unavailable) are recovered and logged per-platform without failing the request or stopping sibling platform writes
- [ ] A `401` on a tag write triggers exactly one re-auth + retry; the cached token is reused across writes otherwise
- [ ] `StateVersion` gains `tags: Option[List[String]]` with the updated spray-json format; no null usage
- [ ] `PlatformMapper` / `DistributionMapper` reused for coordinate translation (no duplicated tables)
- [ ] Cross-path platform-scope asymmetry documented in code/comments where relevant
- [ ] Cucumber acceptance scenarios above pass; `sbt test` green and scalafmt clean
