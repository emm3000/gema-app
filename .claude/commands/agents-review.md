---
description: Self-review pending changes against the repo rules
allowed-tools: Bash(git status:*) Bash(git diff:*) Read Grep
disable-model-invocation: true
---

Review the pending changes (staged + unstaged) against `CLAUDE.md` and `.claude/rules/`. Use `git status`, `git diff` and `git diff --cached` to see what changed.

## Checklist

1. **Layer boundaries**
   - Any file under `core/domain/` or `core/siagie/` importing Android or SQLDelight types?
   - Any file under `core/` importing a feature or `app` package, or a feature module importing another feature module?
   - Allowed dependencies: `app -> data`, `app -> domain`, `data -> domain`.

2. **MVI**
   - New features have `UiState`, `UiIntent`, `UiEffect`, and `onIntent(intent)`?
   - Naming: `*ViewModel`, `*Route`, `*UiState`, `*UiIntent`, `*UiEffect`?

3. **UI**
   - Any direct use of raw Material3 (`OutlinedTextField`, `Button`, `TextField`)?
   - New shared components use the `G` prefix and live in `core/ui/`?

4. **Detekt (config/detekt/detekt.yml)**
   - Nesting ≤ 3?
   - No nested `also/apply/run/let`?
   - ≤ 5 returns per function (excluding labeled returns)?

5. **Offline-first**
   - Any code assuming a backend, remote sync, or a login flow?
   - Writes go through `GemaDb`?

6. **Hygiene**
   - Obvious or "what it does" comments instead of "why"?
   - Sensitive files in the diff (`keystore.properties`, `local.properties`, `key/`)?

## Output

For each violation: `file:line` + rule + suggestion on a single line. If everything is clean, reply **"clean — ready to commit"**. Do not edit files in this turn.
