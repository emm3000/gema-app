---
description: Run detekt and unit tests, summarize failures
allowed-tools: Bash(./gradlew:*) Read
disable-model-invocation: true
---

Run the standard pre-commit checks for this Android repo:

1. `./gradlew detekt` — report any style violations.
2. `./gradlew testDebugUnitTest` — report any failing tests.
3. `./gradlew checkModuleBoundaries` — report any layer violation.

`testDebugUnitTest` reaches the JVM-only modules too: `gema.jvm.library` registers that task name as an alias for `test`.

Group findings by module (`:app`, `:core:*`, `:feature:*`). For each violation include `file:line` and the rule/test name.

Do NOT fix anything in this turn — only report. End with one line: **"ready to commit"** if both pass, or a short list of what to fix next.
