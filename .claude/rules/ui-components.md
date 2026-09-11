---
paths:
  - "core/ui/src/main/kotlin/**"
  - "feature/*/src/main/kotlin/**"
---

# Shared UI rules

Every shared component lives in the `core:ui` module, under `com.emm.gema.core.ui`, with the tokens under `com.emm.gema.core.theme`. The components are inspired by shadcn/ui and define the app's theme.

## The iron rule

Feature screens call **only** `G*` components. **Never** raw Material3 — no `Button`, `OutlinedTextField`, `TextField`, `Card`, `IconButton`, `Text`.

A custom component written inside a screen never replaces a `core/ui` component that exists for that purpose. If the `core/ui` one does not fit, extend or modify it first.

## Naming

- **Public composables**: `G` prefix — `GInput`, `GButton`, `GChip`, `GCard`, `GIconButton`. Hard rule.
- **Files**: match the component name (`GTopBar.kt`, `GSearchBar.kt`, `GEmptyState.kt`). A new standalone shared component gets a `G`-prefixed file.

## Before creating a component

1. **Check `core:ui` first.** If it exists, use it. No exceptions.
2. **Decide the scope.** Used by a single screen, it belongs to the feature package (`feature/<Feature>/`). Used app-wide, it belongs in `core:ui`.
3. **If it must be created**, template on `GButton.kt` — `GemaShapes.control`, `GemaSpacing.minimumTouchTarget` — and include a `@PreviewLightDark` preview.

## Theme tokens are the style guide

`Color.kt`, `Type.kt` and `Foundation.kt` under `com.emm.gema.core.theme` are the single source of truth for every visual value. There is no separate style-guide document holding hex codes, and none should be created — a second copy drifts.

- Never hardcode a `Color(0x...)`, a `.sp` size or a `.dp` padding in a feature screen. Add or reuse a token.
- Spacing comes from `GemaSpacing`; the screen gutter is `GemaSpacing.screenGutter`. Do not re-declare a local constant for it.
- Shapes come from `GemaShapes`: `control` for buttons, inputs and chips, `container` for cards and sheets, `pill` where a full round is meant.

## Never

- Raw Material3 in a feature screen, including a bare `Text`.
- A literal color, size or radius outside `com.emm.gema.core.theme`.
- Semantic colors (success/warning/destructive) used to mean anything other than a system state — never to carry a grade or a right/wrong result.
- An illustrated mascot in an empty state.
- Emoji as an icon. Icons are vector assets or drawn paths.
