---
allowed-tools: Bash(git add:*), Bash(git status:*), Bash(git commit:*), Bash(git diff:*), Bash(git log:*), AskUserQuestion
description: Analyze all staged and unstaged changes, split into minimal logical commits following conventional commit convention, confirm with user before executing
---

## Context

- Current git status: !`git status`
- Staged changes: !`git diff --staged`
- Unstaged changes: !`git diff`
- Recent commits: !`git log --oneline -5`

## Your task

Analyze all staged and unstaged changes and propose a minimal set of logical commits following the conventional commit convention. Then confirm with the user before executing.

### Step 1: Analyze and group

Read the diffs carefully. Group files by their **logical purpose** — what problem they solve or feature they relate to, not just their file type. A single file can only belong to one commit.

Commit type prefixes to use:

- `feat:` — new feature or functionality
- `fix:` — bug fix
- `chore:` — maintenance, config, dependency, build changes
- `docs:` — documentation only
- `refactor:` — code restructuring without behavior change
- `style:` — formatting, whitespace, missing semicolons
- `test:` — adding or modifying tests
- `ci:` — CI/CD pipeline configuration

Do NOT add a scope in parentheses — use plain type prefixes only (e.g., `feat: Add OAuth login`, never `feat(auth): Add OAuth login`).

Aim for the **minimum number of commits** that still separates meaningfully different concerns. Don't split a single cohesive change just to have more commits.

### Step 2: Propose commits

Output a numbered list of proposed commits. For each commit show:

1. The commit message (one line, e.g., `feat: Add login component`)
2. The files to be included

Example output format:

```
Proposed commits:

1. feat: Add login form component
   - apps/web/components/auth/LoginForm.tsx
   - apps/web/app/(auth)/login/page.tsx

2. chore: Update .gitignore for MCP and shrimp data
   - .gitignore
```

### Step 3: Ask for confirmation

Use AskUserQuestion to ask the user whether to proceed with the proposed commits as-is, or whether they want to make changes.

### Step 4: Execute

Once the user confirms, execute each commit in order:

1. `git add <files>` — stage only the specific files for this commit
2. `git commit -m "<message>"` — commit with the exact message

**Commit message rules:**

- The verb after `type:` **must start with a capital letter** (e.g., `feat: Add login`, not `feat: add login`)
- The message must be a single line
- Do NOT add any trailers, footers, or signatures such as "Co-Authored-By", "Signed-off-by", or any other generated text
- Only the conventional commit message itself
