---
description: Ralph Wiggum raise-PR — open or update the pull request for the completed feature branch using the gh CLI.
argument-hint: "[goal]"
---

# Raise PR Agent

The build and review phases are done. Open a pull request for this feature branch — or update the existing one — using the **`gh`** CLI.

## Goal

$ARGUMENTS

## Context

- **Base branch:** `$BASE_BRANCH`
- **Head branch:** the current branch — `git branch --show-current`

## Phase 1: Guard (fail fast)

Print a clear reason and exit WITHOUT creating or editing a PR if either holds:

1. **Nothing to ship** — `git rev-list --count "$BASE_BRANCH"..HEAD` is `0`. No commits exist to propose.
2. **Incomplete work** — `IMPLEMENTATION_PLAN.md` still has incomplete items. Count them with `grep -c '^- \[ \]' IMPLEMENTATION_PLAN.md 2>/dev/null || echo 0` and read the **number it prints**, not the exit code (`grep -c` exits non-zero when the count is `0`, and the `|| echo 0` also covers a missing file). If that number is greater than `0`, refuse to raise a partial-work PR and report how many remain.

When a guard trips, let the node fail — do not open a PR.

## Phase 2: Compose

Use **Sonnet** subagents to gather material, then write a clear title and body:

- **Title** — a concise Conventional-Commits-style summary of the feature (from the goal + `specs/`).
- **Body** — Markdown with these sections:
  - `## What & why` — the feature's intent, distilled from `specs/`.
  - `## Changes` — the salient changes, grouped, from `git log "$BASE_BRANCH"..HEAD --oneline`.
  - `## Review` — the review outcome: gaps found and then closed, or "spec fully satisfied". Read the `## Review gaps` section of `IMPLEMENTATION_PLAN.md`.
  - `## Test plan` — how to verify, using the project's build/test commands (from `AGENTS.md`/`CLAUDE.md`).

Write the body to `"$ARTIFACTS_DIR/pr-body.md"` and pass it with `--body-file` to avoid shell-escaping problems.

## Phase 3: Create or update (idempotent)

- Look for an existing open PR for the head branch:
  `gh pr list --head "$(git branch --show-current)" --state open --json number,url`
- If one exists, **update** it:
  `gh pr edit <number> --title "<title>" --body-file "$ARTIFACTS_DIR/pr-body.md"`
- Otherwise **create** it (gh uses the current branch as head):
  `gh pr create --base "$BASE_BRANCH" --title "<title>" --body-file "$ARTIFACTS_DIR/pr-body.md"`

## Output

Print the PR URL on the last line — the `report` node consumes `$raise-pr.output`:

```
PR: <url>
```

## Constraints

- Use the **`gh`** CLI for all GitHub operations. Do NOT push code or modify the working tree.
- Never mark plan items complete or edit `PROGRESS.md`.
- **Idempotent** — re-running must update the existing PR, never open a duplicate.
