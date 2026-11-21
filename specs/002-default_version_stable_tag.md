# Mark the Default with `stable`, not `lts`

Since 4.7.46, vendor-release copies a non-java candidate's default version into the State API as the `lts` tag ([`001-default_version_lts_tag_dual_write.md`](001-default_version_lts_tag_dual_write.md)). This change makes that tag `stable`. Nothing else about the dual write changes: both paths, the java skip, the best-effort recovery and the token retry stay as 001 describes them.

The reason is `sdkman-state` [`022-non_java_version_rules.md`](../../../do/sdkman-state/specs/022-non_java_version_rules.md). From 022 on, State accepts only `stable` and `latest` on a non-java version, derives the candidate default from `stable` alone, and refuses `lts` with `400`. vendor-release swallows State failures, so a vendor still sending `lts` would get a `201` or `202` while its default never reaches State.

## Requirements

- `PUT /candidates/default` for a non-java candidate asserts `stable` on every platform row of `(candidate, version)` through `POST /versions/tags`, where it asserted `lts`.
- `POST /versions` with `"default": true` for a non-java candidate sends `tags: ["stable"]` in the State version payload, where it sent `["lts"]`. A non-default release still sends no `tags` field.
- vendor-release never sends `lts` to the State API.
- This change ships before `sdkman-state` 022. That is safe on its own, because State's current default cascade already reads `stable` before `lts`.
- Once 022 is live, vendor-release is never rolled back below this release: an older build sends `lts`, State refuses it, and the vendor never sees the failure. Roll forward instead.

## Rules

- rules/scala-rules.md
- rules/ddd-rules.md

## Extra Considerations

- **Old `lts` tags are left alone.** Defaults set before this change hold `lts` in State. vendor-release does not clear them: the next default it sets places `stable`, which the cascade reads first, and 022's data migration retags the rest.
- **Path 2 tag semantics follow State.** Before 022, State's `POST /versions` replaces a row's tag set, so a default release that re-posts an existing row drops any other tag on it, `latest` included. That is today's behaviour with `lts` and this change does not alter it. From 022 on, the field removes no tag.

## Implementation Notes

The whole change is the tag constant and the names and tests built around it:

- `HttpStateApiClient.LtsTag` (`"lts"`) becomes `StableTag` (`"stable"`), with its scaladoc rewritten; both call sites, in `CandidateDefaultRoutes` and `VersionReleaseRoutes`, use it.
- `propagateLtsTag` and the comments that say `lts` are renamed to `stable`.
- The two `*_lts_tag.feature` files become `*_stable_tag.feature` and assert `stable`; the `StubSteps` step that pins `tags ["lts"]` pins `["stable"]`; `HttpStateApiClientSpec` uses `StableTag`.

## Specification by Example

```gherkin
Feature: Dual-write default version as stable tag

  Background:
    Given the consumer has a valid auth token
    And the state API is available

  Scenario: Setting a non-java default asserts the stable tag
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And an existing UNIVERSAL groovy version 2.3.6 exists
    And the consumer for candidate groovy is making a request
    When a JSON PUT on the /candidates/default endpoint:
    """
          |{ "candidate" : "groovy", "version" : "2.3.6" }
    """
    Then the status received is 202 ACCEPTED
    And the state API received a POST /versions/tags with tag stable for groovy 2.3.6 UNIVERSAL
    And the state API received 1 POST /versions/tags requests

  Scenario: Releasing a non-java version as default sends the stable tag
    Given the existing default UNIVERSAL groovy version is 2.3.5
    And the consumer for candidate groovy is making a request
    And the URI /groovy-2.3.6.zip is available for download
    When a JSON POST on the /versions endpoint:
    """
          |{ "candidate" : "groovy", "version" : "2.3.6",
          |  "url" : "http://localhost:8080/groovy-2.3.6.zip",
          |  "platform" : "UNIVERSAL", "default" : true }
    """
    Then the status received is 201 CREATED
    And the state API received a POST /versions payload containing tags ["stable"]
```

## Verification

- [x] `PUT /candidates/default` (non-java) asserts `stable` on every platform row
- [x] `POST /versions` with `default: true` (non-java) sends `tags: ["stable"]`
- [x] No `lts` is left in vendor-release's main or test sources outside the 001 spec
- [x] Every 001 scenario still passes, renamed to `stable`; `sbt test` green and scalafmt clean
