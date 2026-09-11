---
paths:
  - "app/src/main/kotlin/**"
  - "core/*/src/*/kotlin/**"
  - "feature/*/src/*/kotlin/**"
---

# Architecture rules

Clean Architecture across the module layout in `CLAUDE.md`. `./gradlew checkModuleBoundaries` enforces the direction; these rules explain it.

## Layers and dependency direction

| Layer | Contains |
|---|---|
| `core:domain` | Pure Kotlin. Models, value objects, use cases, and the **interfaces** the outer layers implement. |
| `core:database` | Implementations of the domain interfaces: SQLDelight, local identity, mappers. |
| `app` / `feature:*` | Presentation. MVI features, Compose UI, navigation, Koin wiring, startup. |

Allowed dependencies, and nothing else:

```
app        -> core:domain, core:database, core:siagie, core:ui, feature:*
feature:*  -> core:domain, core:ui
core:database -> core:domain
core:siagie   -> core:domain
```

- `core:domain` is **JVM-only**. No Android imports, no SQLDelight, no `Context`. If a use case needs the current time, it takes a `Clock`; it does not call `System.currentTimeMillis()`.
- `core:database` never depends on `app` or a feature module, and no feature module depends on another.
- `GemaDb` is the source of truth for reads and writes.

## Dependency inversion is the seam

The domain declares the contract; the infrastructure obeys it. The domain never imports an implementation.

Repository interfaces live in `core:domain`. Implementations live in `core:database`.

## Each layer owns its own model

A database entity, a network DTO, a domain model and a `UiState` are four different things even when their fields match. Mappers convert between them.

- A SQLDelight row never reaches a `UiState`.
- A domain model never carries presentation concerns (formatted strings, resource ids, colors).

This is not duplication to be removed. See `principles.md`, DRY.

## MVI contract

Naming lives in `naming.md`. This is the flow.

- **One state object per feature.** `<Feature>UiState` is a `data class` with every field `val`. There is no second source of screen state.
- **One public entry point.** The ViewModel exposes `onIntent(intent: <Feature>UiIntent)`. Nothing else is public except the state and effect streams.
- **State is a `StateFlow`, effects are one-shot.** Effects (navigation, toasts, dialogs) are emitted once and consumed once. An effect is never stored in `UiState`, because state replays on recomposition and configuration change and would fire the effect twice.
- **The screen is stateless.** `<Feature>Screen` receives the state and a lambda that dispatches intents. It never holds business state, never touches a repository, never touches a use case, never injects a ViewModel.
- **The route wires everything.** `<Feature>Route` obtains the ViewModel through Koin, collects state, passes the dispatch lambda down, and consumes effects.
- **Intents describe what the user did**, not what the ViewModel should do. Local UI state that has no business meaning — a card's flipped face, an expanded section — may stay as `remember` inside the composable.

## ViewModels never hold literal UI copy

A ViewModel never carries a literal string of user-facing copy, Spanish or otherwise, in state or effects. It emits an enum or another typed value; the screen resolves that value to text with `stringResource` at the UI layer.

This keeps copy in one place (`res/values/strings.xml`), keeps ViewModels free of locale concerns, and keeps assertions in ViewModel tests about *what happened*, not about *which string was chosen*.

Reference pattern: `feature:backup`. `BackupMessage` is a plain enum of message kinds; `BackupViewModel` emits `BackupUiEffect.ShowMessage(BackupMessage.BACKUP_CREATED)`, never a string; `BackupRoute` builds a `Map<BackupMessage, String>` with `stringResource(...)` per entry and resolves the enum to text via `messages.getValue(effect.message)`.

## When a new dependency crosses a layer

Before adding a dependency to any package, check the direction above. If the change needs `core:domain` to reach outward, the design is wrong: invert it with an interface in `core:domain`.
