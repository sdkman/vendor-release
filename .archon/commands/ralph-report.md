---
description: Ralph Wiggum run report — read-only factual summary of the plan/build/review/PR outputs.
---

# Ralph run — completion report

The Ralph loop has finished — built, reviewed, fixed, and a PR raised. Produce a concise, factual report.

**Goal:** $ARGUMENTS

**Build budget vs items:**

$plan-count.output

**Last build iteration output:**

$build.output

**Review outcome:**

$review.output

**Pull request:**

$raise-pr.output

## Steps

1. `cat IMPLEMENTATION_PLAN.md` — note which items are complete vs still `- [ ]`, including the `## Review gaps` section.
2. `git log --oneline -20` — commits produced during this run.
3. `tail -n 60 PROGRESS.md` — latest learnings and gotchas.

## Output format

```
═══════════════════════════════════════════════
RALPH WIGGUM — RUN REPORT
═══════════════════════════════════════════════
Goal:    {goal or "none given"}
Branch:  {current branch}

Plan:    {N complete} / {M total} items   (budget was {budget})
Remaining items:
- {each still-incomplete item, or "none"}

Review:  {gaps found and closed, or "spec fully satisfied"}
PR:      {pull request URL}

Commits this run:
{git log output}

Learnings (from PROGRESS.md):
- {key patterns / gotchas}
═══════════════════════════════════════════════
```

Keep it factual — just the data, no commentary. Do NOT modify any files.
