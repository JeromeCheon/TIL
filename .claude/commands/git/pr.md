---
allowed-tools: Bash(git log:*), Bash(git diff:*), Bash(git branch:*), Bash(gh pr create:*), Read
description: Create a GitHub PR to main by default, or to a specified branch (e.g. /git:pr feature-branch). Checks CLAUDE.md and README freshness before creating the PR.
---

## Context

- Current branch: !`git branch --show-current`
- Commits vs main: !`git log main..HEAD --oneline`
- Diff stat: !`git diff main...HEAD --stat`
- Full diff: !`git diff main...HEAD`
- PR template: !`cat .github/pull_request_template.md 2>/dev/null || echo "(no template)"`

## Task

Target branch: `$ARGUMENTS` if given, otherwise `main`.

### Step 0 — Doc freshness check

Read the full diff and check whether any CLAUDE.md or README.md files need updating.

1. **Identify relevant doc files** based on changed paths:
   - Any change → read `CLAUDE.md`, `README.md`
   - `apps/web/` changes → also read `apps/web/CLAUDE.md`, `apps/web/README.md`
   - `apps/web/lib/notion/` changes → `apps/web/lib/notion/CLAUDE.md`
   - `apps/web/lib/supabase/` changes → `apps/web/lib/supabase/CLAUDE.md`
   - `apps/web/scripts/` changes → `apps/web/scripts/CLAUDE.md`

2. **Check for staleness** — flag if the diff contains:
   - New env vars not listed in `CLAUDE.md` env vars section
   - New `package.json` dependencies absent from `README.md` tech stack
   - New route files under `app/[locale]/` not in the routes table
   - New directories or files in `lib/` / `scripts/` / `hooks/` not reflected in any CLAUDE.md
   - Directory structure changes not reflected in README

3. **Act on findings**:
   - Nothing stale → proceed to Step 1.
   - Something stale → use `AskUserQuestion` to list exactly which file/section needs updating. Ask the user to update the docs, make a commit, then confirm before proceeding.

### Step 1 — Create PR

1. Read the commits and diff to understand what changed and why.
2. Fill the PR template — check relevant boxes (`[x]`), write Korean prose where appropriate.
3. Write a concise English title in conventional commit style (≤72 chars).
4. Run `gh pr create --base <target> --title "<title>" --body "<body>"` and return the PR URL.
