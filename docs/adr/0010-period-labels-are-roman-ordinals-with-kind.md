---
status: accepted
---
# Period labels are a Roman ordinal plus their kind

A Period is shown to the Teacher as "I Bimestre" … "IV Bimestre", or "I
Trimestre" … "III Trimestre", never as a bare number or an English word.
`PeriodKind.labelFor(ordinal)` in `core:domain` looks up the ordinal in a fixed
`listOf("I", "II", "III", "IV")` and appends `kindLabel()` ("Bimestre" /
"Trimestre"). This matches the open question
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
- The label is the Period's name, not screen copy: "I Bimestre" is what the
  Teacher, the UGEL and SIAGIE all call it. It therefore lives once in
  `core:domain/schoolyear/PeriodLabels.kt`, next to `Area.officialName` and
  `UnworkedComment.officialText`, and every feature module reads it from there.
  `Grade.label()` and `Section.title()` sit in
  `core:domain/section/SectionLabels.kt` for the same reason.
- A copy per feature module was the first shape, and four modules needed one
  before the third ticket landed. A feature module may not reach another feature
  module, so the only place a shared name can live is `core:domain`.
