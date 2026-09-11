# CLAUDE.md

Operating manifest for this repo. Loaded in every session.

## Mandatory state

The product is an **offline-first, single-device** gradebook. Don't assume a backend, remote sync, pairing, or a login flow.

`GemaDb` is the source of truth for reads and writes.

## Modules

Currently **one module, `:app`**. A later foundation ticket splits it into:

```
app -> data
app -> domain
data -> domain
```

- `app` — UI, navigation, DI, startup, feature screens
- `data` — repositories, SQLDelight, mappers
- `domain` — **JVM-only** models and use cases. No Android, no DB, no network.

Until the split lands, honor the same boundary through package structure under `app/src/main/kotlin/com/emm/gema/{domain,data,feature}`. A `domain` package file may not import Android or SQLDelight types even while it lives inside `:app`.

## Product

An offline-first gradebook for Peruvian primary teachers. Grades follow the CNEB curriculum and the literal scale (AD / A / B / C). Exports are compatible with SIAGIE. No backend, no login — the device is the only source of truth.

## Non-negotiable rules

These bind on every change, including a new file created before any Kotlin has been read.

- **No comments.** No KDoc, no `//`, no banners, no commented-out code. The code explains itself or it gets renamed. Three narrow exceptions in `.claude/rules/kotlin-style.md`.
- **Explicit types** on every property and local `val` / `var`, and the supertype when the abstraction is what matters. Omit only when the right-hand side is a constructor call that already names the type.
- **Only `core/ui/G*` components** in feature screens (target convention — see `.claude/rules/ui-components.md`). Never raw Material3.
- **MVI per feature**: one `UiState` (all `val`), one `onIntent(intent)` entry point, effects consumed once and never stored in state.
- **`domain` stays JVM-only**, package or module. If it needs to reach outward, invert with an interface in `domain`.
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

Kotlin, Jetpack Compose, Material3, Koin, SQLDelight. Retrofit is present today and slated for removal once the backend-facing code is retired in the foundation ticket — do not build new features on it.

## Commands

- `./gradlew detekt` — style and complexity gate.
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

### Domain docs

Single-context: `CONTEXT.md` and `docs/adr/` at the repo root. See `docs/agents/domain.md`.
