---
status: accepted
---
# SIAGIE import merges by Student Code and never deletes

The Teacher imports the SIAGIE grades template of a Section to bring Students in
with exact names, Student Codes and SIAGIE Ids (US 16-19, 24). A Section is
re-imported whenever SIAGIE changes: a child arrives, a name is corrected, a
child leaves. Attendance and Period Levels are already attached to the Students
of that Section, so the merge decides whether a year of records survives.

The Student Code is the only stable identity across imports: SIAGIE assigns it
once and keeps it across years and schools. The merge is therefore keyed on it,
and nothing else:

- A code in the file and not in the Section creates a Student.
- A code in both updates the name and the SIAGIE Id of the existing Student, and
  clears a withdrawal date, so a returning child is the same row, not a new one.
- A code in the Section and not in the file is **proposed** as Withdrawn, with a
  per-name checkbox the Teacher can clear. Nothing is ever deleted.
- A re-import of an unchanged file produces an empty plan, so importing twice is
  indistinguishable from importing once.

The preview is computed before any write and shown in full — creates, updates
and proposed withdrawals — because the Teacher is the one who knows whether the
file is the right one. Applying re-reads the file and re-plans, so the decision
is taken against the state the write actually starts from.

Students, the withdrawals and the stored file land in one SQLite transaction
(`SiagieImportStore`). A half-applied import would leave a Section that matches
no file, and the Export in ADR 0002 reads that file.

## Matching the file to the Section

A file that does not belong to the Section must be rejected before the preview
(US 24). The generated template carries the Grade in its title row and in its
file name (`6 Primaria EBR.xlsx`). It does not, in the structure we have
documented, carry the Section letter.

So the rejection compares the Grade always, and the Section name only when the
file names one. A file whose Grade differs is refused with both values. A
6th-grade file imported into another 6th-grade Section is accepted, because
nothing in the file contradicts it.

## Consequences

- Editing a Student Code by hand breaks the link to SIAGIE; `StudentForm`
  already warns when a Student carries a SIAGIE Id.
- Two Sections of the same Grade can receive the same file. Until a real
  generated file proves the Section is written somewhere, that ambiguity is the
  Teacher's to resolve, and the preview is what makes it visible.
- `student.insert` is `INSERT OR REPLACE`, so it upserts on the primary key, and
  a plan that creates and updates in the same transaction works. That statement
  also resolves the `UNIQUE (section_id, student_code)` index by **deleting** the
  row it collides with. The planner never produces such a collision, because it
  compares the codes in the file against every enrolled Student, the Withdrawn
  ones included. Any future writer that inserts Students outside the planner has
  to keep that invariant, or a Student disappears without an error.
- The fixture under `core/siagie` is modelled from the Minedu instructives, not
  produced by SIAGIE. The Grade and Section extraction must be re-checked
  against a real file before release, together with the Export in ADR 0002.
