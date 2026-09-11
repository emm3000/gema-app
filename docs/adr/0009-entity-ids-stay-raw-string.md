---
status: accepted
---
# Entity ids stay raw `String` until a dedicated migration

`docs/design/screens.md` specifies typed ids (`SchoolYearId`, `SectionId`,
`StudentId`, `PeriodId`, `CompetencyId`), but every entity in `core:domain`
(`Section`, `Student`, `Period`, `SchoolYear`, `Competency`) still declares
its id as a raw, `require`-validated `String`. This was set by the earliest
domain rewrite (#2) and carried forward consistently through backup (#5) and
setup (#4) rather than migrated partway through an unrelated feature PR.
Issue #27 tracks introducing Kotlin value classes for every entity id,
mapped at the SQLDelight boundary, as one dedicated change with no feature
work mixed in.

## Consequences

- Every use case, repository and mapper written before #27 lands takes and
  returns `String` ids; a reviewer should not expect `StudentId`-style types
  yet even though `screens.md` shows them.
- `principles.md` ("make illegal states unrepresentable") is knowingly
  unmet on this one axis until #27, traded for not half-migrating types
  across features that were mid-flight.
- #27 is scoped as a single serial PR precisely because the inconsistency is
  uniform today: fixing it piecemeal, feature by feature, would leave some
  layers typed and others not for longer, which is worse than the current
  consistent gap.
