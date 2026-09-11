---
paths:
  - "app/src/main/kotlin/**/feature/**/*.kt"
  - "app/src/main/kotlin/**/core/ui/**/*.kt"
  - "app/src/main/kotlin/**/core/theme/**/*.kt"
  - "feature/**/*.kt"
---

# Shared UI rules

Every shared component lives in `core/ui/` (target path: `app/src/main/kotlin/com/emm/gema/core/ui/`, moving to its own module once the foundation split lands). The components are inspired by shadcn/ui and define the app's theme.

## The iron rule

Feature screens call **only** `G*` components. **Never** raw Material3 — no `Button`, `OutlinedTextField`, `TextField`, `Card`, `IconButton`.

A custom component written inside a screen never replaces a `core/ui` component that exists for that purpose. If the `core/ui` one does not fit, extend or modify it first.

## Naming

- **Public composables**: `G` prefix — `GInput`, `GButton`, `GChip`, `GCard`, `GIconButton`. Hard rule.
- **Files**: match the component name (`GTopBar.kt`, `GSearchBar.kt`, `GEmptyState.kt`). A new standalone shared component gets a `G`-prefixed file.
- **Exception without a `G*` composable**: `FieldShell` — an internal building block and the template for inputs.

## Before creating a component

1. **Check `core/ui/` first.** If it exists, use it. No exceptions.
2. **Decide the scope.** Used by a single screen, it belongs to the feature package (`feature/<Feature>/`). Used app-wide, it belongs in `core/ui/`.
3. **If it must be created**, template on `Input.kt` + `FieldShell.kt` — animated border, transparent background, 48dp minimum touch target — and include a `@PreviewLightDark` preview.

## Theme tokens are the style guide

`core/theme/Color.kt`, `Type.kt` and `Foundation.kt` are the single source of truth for every visual value. There is no separate style-guide document holding hex codes, and none should be created — a second copy drifts.

- Never hardcode a `Color(0x...)`, a `.sp` size or a `.dp` padding in a feature screen. Add or reuse a token.
- Spacing comes from `GemaSpacing`; the screen gutter is `GemaSpacing.screenGutter`. Do not re-declare a local constant for it.
- Shapes come from `GemaShapes`: `control` for buttons, inputs and chips, `container` for cards and sheets, `pill` where a full round is meant.

## Never

- Raw Material3 in a feature screen.
- A literal color, size or radius outside `core/theme/`.
- Semantic colors (success/warning/destructive) used to mean anything other than a system state — never to carry a grade or a right/wrong result.
- An illustrated mascot in an empty state.
- Emoji as an icon. Icons are vector assets or drawn paths.
