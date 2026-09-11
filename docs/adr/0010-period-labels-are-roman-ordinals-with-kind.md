---
status: accepted
---
# Period labels are a Roman ordinal plus their kind

A Period is shown to the Teacher as "I Bimestre" … "IV Bimestre", or "I
Trimestre" … "III Trimestre", never as a bare number or an English word.
`PeriodKind.labelFor(ordinal)` in each feature module's `PeriodLabels.kt`
looks up the ordinal in a fixed `listOf("I", "II", "III", "IV")` and appends
`kindLabel()` ("Bimestre" / "Trimestre"). This matches the open question
raised against `screens.md`'s wireframe copy ("Los periodos II y III se
superponen"), which already used Roman ordinals informally.

## Considered Options

- **Plain cardinal number** ("Periodo 2"). Rejected: it does not match the
  wireframe copy the Teacher-facing screens were designed against, and it
  drops the distinction between a bimester and a trimester school year.
- **Spelled-out ordinal** ("Segundo Bimestre"). Rejected: longer, and does not
  fit the compact chips and dropdown badges (`docs/design/components.md`)
  that carry the label at 360dp.

## Consequences

- The label always carries both the ordinal and the kind, so "II" alone is
  never shown without saying whether the school year is divided into
  bimesters or trimesters.
- `PeriodKind` itself stays presentation-agnostic in `core:domain`; the
  Roman-ordinal formatting lives in each feature package's own
  `PeriodLabels.kt`, consistent with `.claude/rules/architecture.md` keeping
  formatted strings out of the domain model.
