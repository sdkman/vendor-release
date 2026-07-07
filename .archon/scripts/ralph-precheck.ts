#!/usr/bin/env bun
/**
 * Ralph PRECHECK node — fail fast unless the repo is ready for a ralph run.
 *
 * The ralph-wiggum workflow does NO git surgery: creating the feature
 * branch and committing the spec are the human's responsibility. This
 * guard enforces that contract up front, so a misconfigured run fails at
 * second one with a clear remedy instead of part-way through the loop.
 *
 * Prerequisites the human must satisfy before running:
 *   1. The base branch is up to date.
 *   2. A feature branch is checked out.
 *   3. The spec is committed on that feature branch.
 *   4. The feature branch is pushed (has an upstream).
 *
 * Checks (any failure exits non-zero with guidance):
 *   1. HEAD is on a named branch (not detached).
 *   2. The current branch is NOT the base branch ($BASE_BRANCH, default
 *      "main") — ralph must run on a feature branch.
 *   3. The current branch has an upstream — i.e. it has been pushed — so
 *      the raise-PR node can open a PR against it.
 *
 * The working tree is intentionally NOT checked for cleanliness: a target
 * project legitimately carries untracked tooling (e.g. an uncommitted
 * `.archon/`, build outputs), and the loop reads `specs/` from disk whether
 * or not the spec is committed. Enforcing a clean tree here would trip on
 * those false positives; committing the spec stays the human's job.
 *
 * Reads $BASE_BRANCH from the environment (Archon injects it into script
 * nodes); ARTIFACTS_DIR is unused. No downstream node consumes
 * $precheck.output — the diagnostics are for humans.
 *
 * Invoked by Archon as a named script (`runtime: bun`); no args, no stdin.
 */

import { execFileSync } from "node:child_process";

const PREREQS = [
  "  1. Ensure the base branch is up to date.",
  "  2. Create a feature branch:   git checkout -b feature/<name>",
  "  3. Commit your spec on it:     git add specs/<name>.md && git commit",
  "  4. Push it:                    git push -u origin HEAD",
].join("\n");

function git(args: readonly string[]): string {
  return execFileSync("git", args, { stdio: ["ignore", "pipe", "pipe"] })
    .toString()
    .trim();
}

function fail(message: string): never {
  console.error(`ralph-precheck: ${message}`);
  process.exit(1);
}

function currentBranch(): string {
  try {
    return git(["branch", "--show-current"]);
  } catch {
    return "";
  }
}

function hasUpstream(): boolean {
  try {
    git(["rev-parse", "--abbrev-ref", "--symbolic-full-name", "@{u}"]);
    return true;
  } catch {
    return false;
  }
}

const baseBranch = (process.env.BASE_BRANCH ?? "").trim() || "main";

const branch = currentBranch();
if (branch.length === 0) {
  fail(`HEAD is detached — not on a branch. Ralph runs on a feature branch:\n${PREREQS}`);
}

if (branch === baseBranch) {
  fail(`on the base branch '${baseBranch}'; ralph must run on a feature branch.\n${PREREQS}`);
}

if (!hasUpstream()) {
  fail(
    `branch '${branch}' has no upstream — push it first so a PR can be raised:\n` +
      `  git push -u origin ${branch}`,
  );
}

console.log(`BRANCH=${branch}`);
console.log(`BASE_BRANCH=${baseBranch}`);
console.log("UPSTREAM=set");
