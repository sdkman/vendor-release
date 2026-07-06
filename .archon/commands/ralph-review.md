---
description: Ralph Wiggum review — scrutinise the built implementation against the specs and append any gaps to IMPLEMENTATION_PLAN.md.
argument-hint: "[goal]"
---

# Review Agent

The build loop has finished. You are a review agent running ONCE in a FRESH session. Your job is to scrutinise what was built against the specifications and record any gaps as new work items. **You do not implement anything** — a second build phase will close whatever you find.

## Goal

$ARGUMENTS

(If the goal above is blank, infer the objectives from `specs/` and `IMPLEMENTATION_PLAN.md`.)

---

## Phase 1: Understand

Gather context. Use parallel **Sonnet** subagents to read concurrently — as many as the work warrants.

- **Operational guardrails** — read `AGENTS.md` or `CLAUDE.md` (if present) for conventions and project rules
- **Specifications** — read everything in `specs/`; this is the source of truth
- **Implementation plan** — read `IMPLEMENTATION_PLAN.md` to see what was claimed complete (`- [x]`)
- **Application source** — read the code that was actually written this run (`git log`, `git diff`) and the surrounding modules
- **Tests** — read the test sources to see what behaviour is actually covered

## Phase 2: Scrutinise

Use an **Opus** reasoning subagent with **ultrathink** to compare the *real, on-disk implementation* against the specs. Be adversarial — assume the build was optimistic.

Look for:
- Spec requirements with no corresponding implementation
- Behaviour implemented but not matching the spec (wrong status codes, edge cases, semantics)
- Missing or weak tests for spec'd behaviour (happy path only, unhappy paths skipped)
- Placeholders, stubs, TODOs, or `- [x]` items that are not genuinely done
- Inconsistencies introduced this run

**Never assume something is missing.** Confirm with a code search before flagging it. A gap is only real if you verified the absence.

## Phase 3: Record gaps

Append your findings to `IMPLEMENTATION_PLAN.md` — **append-only**:

- Add a heading `## Review gaps (YYYY-MM-DD)` using today's date (`date +%Y-%m-%d`).
- Under it, list each gap as an incomplete item (`- [ ]`) following the **Entry Format** documented at the top of `IMPLEMENTATION_PLAN.md` (title, Scope, Files, Done when).
- **Never** alter, re-order, or un-check existing items — the plan is an append-only ledger. Only append.
- If you find **no** gaps, append the heading with a single line `- none — spec fully satisfied` (not a `- [ ]` item, so it adds no build work) and stop.

---

## Constraints

- **Review only. Do NOT implement, fix, or refactor anything.**
- **Append-only.** Never modify existing plan items or `PROGRESS.md` entries.
- Never assume functionality is missing — confirm with a code search first.
- Keep gap items concrete and verifiable so the build phase can close them.

---

## Output

End with a concise summary — the `report` node consumes `$review.output`:

```
REVIEW: <N> gap(s) appended to IMPLEMENTATION_PLAN.md
- <each gap title, or "none — spec fully satisfied">
```
