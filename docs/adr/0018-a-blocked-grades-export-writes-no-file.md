---
status: accepted
---
# A blocked grades export writes no file at all

Validation of a SIAGIE grades export runs in `core:domain` before any byte is
written. `GetGradesExportPlanUseCase` walks the active Areas of the Section and,
for each one, the Worked Competencies of the selected Period, and returns both
the Export Gaps and the cells that would be written. `ExportGradesUseCase` asks
for that plan first: while a single `C` lacks its Descriptive Conclusion the
result is `Blocked` with the full list of Student x Competency gaps, and the
workbook writer is never called.

The workbook writer answers with the same discipline. When an active Area has no
sheet in the stored Template, or a Student with a recorded level is not in its
roster, it returns those Areas and Student Codes instead of a file, and
`ExportGradesUseCase` turns them into a `TemplateMismatch` naming the Areas and
the Students. Filling what fits and dropping the rest would hand the Teacher a
file that looks finished and is not.

A recorded level reaches the file as it stands: `AD`, `A`, `B`, `C` or the
`Comentario N` token of an Unworked Comment, plus the Descriptive Conclusion in
the next column when it is not blank. Cells of a Competency that was not marked
as worked, of a hidden Area, or of a Student with no level are left exactly as
SIAGIE generated them.

## Considered Options

- **Write the file and list the gaps afterwards.** Rejected: SIAGIE accepts a
  partially filled upload, so a Teacher would file an incomplete Period without
  noticing, and US 49 asks for no partial file.
- **Fill what the Template can hold and ignore the rest.** Rejected: the drop
  is silent at exactly the moment the Teacher stops checking. A Template from
  another Section, or one imported before a Student enrolled, would file a
  Period with Students missing and nothing on screen would say so.
- **Block only the offending sheet.** Rejected: one upload is one Period for one
  Section (ADR 0012); half a file is still a wrong file, and the Teacher would
  have to remember which Areas made it through.

## Consequences

- The Export screen enables *Generar archivo* only when the plan is ready, and
  lists every gap as a tap-through row that opens the Period Levels grid.
- Export validation and the cell mapping live in different modules:
  `core:domain` owns which cells should be written, `core:siagie` owns where
  they land in the workbook.
- A Section with no stored Imported Template reports `Unavailable` before
  validation runs at all; there is nothing to fill.
- A Template that no longer matches the Section reports `TemplateMismatch` with
  the Areas and Students it cannot hold; the fix is re-importing the Template
  SIAGIE generated for that Section.
