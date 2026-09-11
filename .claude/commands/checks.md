---
description: Run detekt and unit tests, summarize failures
allowed-tools: Bash(./gradlew:*) Read
disable-model-invocation: true
---

Run the standard pre-commit checks for this Android repo:

1. `./gradlew detekt` — report any style violations.
2. `./gradlew testDebugUnitTest` — report any failing tests.

Until the foundation split lands there is only `:app`, so both tasks cover the whole codebase. Once `domain` and `data` become separate JVM/Android modules, add their `test` tasks explicitly here, the same way `testDebugUnitTest` never reaches a JVM-only module.

Group findings by module (`:app`, and later `:data`, `:domain`). For each violation include `file:line` and the rule/test name.

Do NOT fix anything in this turn — only report. End with one line: **"ready to commit"** if both pass, or a short list of what to fix next.
