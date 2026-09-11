---
status: accepted
---
# Level pickers take a plain letter, not an enum

`GLevelChip` and `GLevelPicker` (`core:ui`) used to take a `GLevelOption`, a
`core:ui`-owned enum shaped exactly like `core:domain`'s `AchievementLevel`
(same four constants, same Spanish descriptions). Converting between the two
needs both types in scope, so it cannot live in `core:domain` (UI-free) or in
`core:ui` (domain-free). Two features needed the conversion —
`feature:evaluation` and `feature:activities` — and a feature module may not
reach another feature module, so each grew its own `LevelOptions.kt` with the
identical `AchievementLevel.toOption()` / `GLevelOption.toAchievementLevel()`
pair.

## Considered Options

- **Move the mapper to `core:domain`.** Rejected: `core:domain` stays
  JVM-only and UI-free; it cannot name a `core:ui` type.
- **Move the mapper to `core:ui`.** Rejected: `core:ui` stays domain-free; it
  cannot name a `core:domain` type.
- **Keep one copy and have the other feature import it.** Rejected: features
  cannot import each other.
- **Change the picker's public type to a plain `String?` letter.** Chosen.
  `AchievementLevel.name` already is the letter ("AD", "A", "B", "C"), so no
  conversion function is needed at all — a feature reads `level?.name` going
  in and `AchievementLevel.valueOf(letter)` coming back, both stdlib calls
  with no project-owned mapper to duplicate.

## Consequences

- `GLevelChip(letter: String?, …)` and `GLevelPicker(selected: String?, onSelect: (String?) -> Unit, …)`
  replace the `GLevelOption` parameter. `GLevelOption` stays in `core:ui` as
  an internal detail — it supplies the four letters and their Spanish
  `contentDescription` to `GLevelPicker`'s option list and to `GLevelChip`'s
  accessibility label — but no longer appears in a public signature.
- Both `feature/evaluation/levels/LevelOptions.kt` and
  `feature/activities/evidence/LevelOptions.kt` are deleted; nothing replaces
  them.
- `ColumnModeBar` and `PeriodLevelsScreen`'s local `ColumnModeBarState` now
  carry `AchievementLevel?` end to end and only read `.name` at the exact
  `GLevelChip` / `GLevelPicker` call, instead of converting one screen away
  from the domain type.
- This only works because `AchievementLevel`'s enum names already are its
  official letters. A future level scale whose UI letter differs from its
  domain constant name would need `AchievementLevel` to expose that letter as
  a property (`core:domain`, still UI-free) rather than reintroducing a
  `core:ui`-typed mapper.
