# CLAUDE.md

Operating manifest for this repo. Loaded in every session.

## Mandatory state

The product is an **offline-first, single-device** gradebook. Don't assume a backend, remote sync, pairing, or a login flow.

`GemaDb` is the source of truth for reads and writes.

## Modules

```
app        -> core:domain, core:database, core:siagie, core:ui, feature:*
feature:*  -> core:domain, core:ui
core:database -> core:domain
core:siagie   -> core:domain
```

- `core:domain` — **JVM-only** models, value objects, use cases and repository interfaces. No Android, no SQLDelight, no network.
- `core:database` — `GemaDb`, the SQLDelight schema, repository implementations, the Android driver and the in-memory JVM driver used by tests.
- `core:siagie` — **JVM-only** SIAGIE workbook import and export.
- `core:ui` — theme tokens and the `G*` components.
- `feature:<name>` — one MVI screen group each: `setup`, `sections`, `students`, `attendance`, `evaluation`, `export`, `backup`.
- `app` — navigation, Koin wiring and startup.

A feature module never depends on another feature module, and only `app` may depend on a feature module. `./gradlew checkModuleBoundaries` fails the build when that is broken.

Shared Gradle configuration lives in convention plugins under `build-logic/`: `gema.android.application`, `gema.android.library`, `gema.android.compose`, `gema.android.feature` and `gema.jvm.library`. A module build file applies its plugin and declares its own dependencies, nothing else. `gradle/libs.versions.toml` is the only place a version is written.

## Product

An offline-first gradebook for Peruvian primary teachers. Grades follow the CNEB curriculum and the literal scale (AD / A / B / C). Exports are compatible with SIAGIE. No backend, no login — the device is the only source of truth.

## Non-negotiable rules

These bind on every change, including a new file created before any Kotlin has been read.

- **No comments.** No KDoc, no `//`, no banners, no commented-out code. The code explains itself or it gets renamed. Three narrow exceptions in `.claude/rules/kotlin-style.md`.
- **Explicit types** on every property and local `val` / `var`, and the supertype when the abstraction is what matters. Omit only when the right-hand side is a constructor call that already names the type.
- **Only `core:ui` `G*` components** in feature screens (see `.claude/rules/ui-components.md`). Never raw Material3.
- **MVI per feature**: one `UiState` (all `val`), one `onIntent(intent)` entry point, effects consumed once and never stored in state.
- **`core:domain` stays JVM-only**. If it needs to reach outward, invert with an interface in `core:domain`.
- **Rebuild, never adapt.** When existing code, config or structure does not fit the target architecture, replace it with a clean implementation. No shims, wrappers or compatibility patches over legacy.
- **`./gradlew detekt testDebugUnitTest` green** before every commit.
- **Never add `Co-Authored-By`** from Claude, Anthropic or any AI assistant to a commit message. Applies to `git commit`, `--amend`, rebases and any generated message flow.

## Detailed rules

Path-scoped, loaded when Kotlin files are touched:

| File | Covers |
|---|---|
| `.claude/rules/architecture.md` | Layer boundaries, dependency inversion, the MVI contract |
| `.claude/rules/naming.md` | Uncle Bob, official Kotlin, naming patterns by layer |
| `.claude/rules/kotlin-style.md` | Explicit types, comment policy, Kotlin idioms, detekt |
| `.claude/rules/principles.md` | YAGNI, KISS, SOLID, DRY with its caveat, what is rejected |
| `.claude/rules/ui-components.md` | The `core/ui` `G*` iron rule, theme tokens |
| `.claude/rules/sqldelight.md` | Schema changes: the three artifacts a migration ships, the migration test |

## Stack

Kotlin, Jetpack Compose, Material3, Koin, SQLDelight. No HTTP client and no serialization library: the device is the only source of truth.

## Commands

- `./gradlew detekt` — style and complexity gate.
- `./gradlew checkModuleBoundaries` — layer boundary gate.
- `./gradlew testDebugUnitTest` — unit tests.
- `./gradlew assembleDebug` — debug build.

## Test stack

JUnit4, MockK, Turbine, Truth, `kotlinx-coroutines-test`. Test names use backtick descriptions (`` `returns error when course id is blank`() ``). Coroutine-driven tests use a `MainDispatcherRule` to swap `Dispatchers.Main`.

## Custom slash commands

- `/checks` — `./gradlew detekt testDebugUnitTest`, failures grouped by module.
- `/feature <Name>` — full MVI scaffold (`UiState` / `UiIntent` / `UiEffect` / `ViewModel` / `Route` / `Screen`).
- `/agents-review` — review the pending diff against these rules.

## Final rule

If a doc contradicts the current code, the code wins and the doc gets updated afterwards.

## Agent skills

### Issue tracker

Issues and specs live in GitHub Issues for `emm3000/gema-app` via the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

Default vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Design and reference docs

- `docs/design/flows.md`, `screens.md`, `components.md` — navigation map, per-screen `UiState` and intents, the `G*` catalog. A feature ticket implements the screen as specified there.
- `docs/cneb/primary.json` — official CNEB areas, competencies and the AD/A/B/C scale. The seed source for `core:domain`.
- `docs/siagie/` — SIAGIE template structure and official instructives.
- `docs/play/` — store listing, privacy policy and Play Console form answers.

### Domain docs

Single-context: `CONTEXT.md` and `docs/adr/` at the repo root. See `docs/agents/domain.md`.
