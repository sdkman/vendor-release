# TODO List

Consider the following rules during execution of the tasks:
- CLAUDE.md (project-specific instructions)

---

## Task 1: Refactor Vendor Suffix Validation Scenarios into Scenario Outline

- [X] Combine vendor suffix rejection scenarios into a comprehensive Scenario Outline

**Prompt**: The feature file `release_version_vendor_suffix_validation.feature` contains three nearly identical scenarios testing vendor suffix validation (lines 53-68, 71-86, and 89-104). These scenarios test that version strings with vendor suffixes `-tem`, `-zulu`, and `-amzn` are properly rejected with a 400 BAD_REQUEST status and the error message "Invalid version format: version field must not contain vendor suffix. Use the 'vendor' field instead."

Refactor these three individual scenarios into a single Scenario Outline with an Examples table that covers all vendor suffix cases comprehensively. The Scenario Outline should:
- Test rejection of versions with vendor suffixes: all vendor suffixes mentioned in `KnownVendorSuffixes` in the `Validation` trait
- Verify 400 BAD_REQUEST status code is returned
- Verify the appropriate error message is returned
- Maintain the same test coverage as the individual scenarios
- Use parameterized values for version numbers and vendor suffixes

**Files affected**:
- `src/test/resources/features/release_version_vendor_suffix_validation.feature`

---

## Task 2: Refactor Pre-release Suffix Validation Scenarios into Scenario Outline

- [X] Combine pre-release suffix acceptance scenarios into a comprehensive Scenario Outline

**Prompt**: The feature file `release_version_vendor_suffix_validation.feature` contains three nearly identical scenarios testing pre-release version suffix validation (lines 125-139, 141-155, and 157-171). These scenarios test that version strings with pre-release suffixes `-RC1`, `-rc-1`, `-SNAPSHOT`, and `-beta-1` are properly accepted with a 201 CREATED status.

Refactor these three individual scenarios into a single Scenario Outline with an Examples table that covers all pre-release suffix cases comprehensively. The Scenario Outline should:
- Test acceptance of versions with pre-release suffixes: -RC1, -SNAPSHOT, -beta-1
- Verify 201 CREATED status code is returned
- Maintain the same test coverage as the individual scenarios
- Use parameterized values for version numbers and pre-release suffixes
- Ensure comprehensive coverage of common pre-release version patterns

**Files affected**:
- `src/test/resources/features/release_version_vendor_suffix_validation.feature`

---

## Execution plan workflow

The following workflow applies when executing this TODO list:
- Execute one task at a time
- Implement the task in **THE SIMPLEST WAY POSSIBLE**
- Run the tests, format and perform static analysis on the code:
    - sbt scalafmtAll
    - sbt test
- **Ask me to review the task once you have completed and then WAIT FOR ME**
- Mark the TODO item as complete with [X]
- Commit the change to Git when I've approved and/or amended the code
- Move on to the next task