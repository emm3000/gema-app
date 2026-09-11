---
status: accepted
---
# Monthly attendance summary counts recorded days; export picks its template at export time

The Teacher reviews a month of Attendance per Student (counts per status) and
exports it into the SIAGIE attendance template, reusing the round-trip
strategy of ADR 0002/0005: fill the real file SIAGIE generated, touch nothing
else.

## Counting

`GetMonthlyAttendanceSummaryUseCase` counts only recorded `AttendanceRecord`
rows per Student per status, for the given month. ADR 0016 already
established that an unmarked day writes no row; this ADR draws the
consequence it flagged: an unmarked day is not counted as anything, not even
as "present". A Student withdrawn before the first day of the month is left
out of the summary entirely, matching how `GetAttendanceDayUseCase` treats a
withdrawal for a single day.

## Export writes a fresh cell only for a day the Teacher actually recorded

`XlsxMonthlyAttendanceWriter` (`core:siagie`) fills one cell per recorded
`AttendanceRecord`: student row (matched by `CodEstudiante`) x day column. A
day with no row gets no edit, so the template cell for that day is left
exactly as SIAGIE generated it. Writing a default status there would be a
claim about a day nobody reviewed, which is exactly what ADR 0016 rejected for
the on-device table; the export must not reintroduce it on the way out.

Status codes (`P`/`T`/`F`/`FJ`) live in one place, `AttendanceSiagieCode`, and
are **not confirmed against a real SIAGIE file** yet — see
`docs/siagie/README.md`.

## The attendance template is picked at export time, not stored

`ImportedTemplateKind.ATTENDANCE` already exists in the domain, and the
`imported_template` table could hold a row for it without a migration. We do
not use it for this feature.

### Considered Options

- **Store the attendance template like the grades template** (ADR 0002):
  import it once from a Students-style preview screen, keep it in
  `imported_template` keyed `(section_id, ATTENDANCE)`, export re-reads it.
  Rejected: the grades template is stable for a school year, so persisting it
  and refreshing on re-import (ADR 0014) fits. The attendance template is a
  **new file every month** — SIAGIE generates one per month, not one per
  Section. `imported_template`'s primary key is `(section_id, kind)`, a single
  slot; a second month would overwrite the first, and a Teacher exporting
  August after September started would silently get September's file. Making
  the key month-aware needs a schema change, and this feature was scoped
  without one.
- **Pick the template file with the system document picker at the moment of
  export, fill it in memory, and hand the result to the share sheet.**
  Adopted. No new table, no migration, no stale file across months: every
  export asks for that month's file, exactly once, and nothing is kept
  afterwards. This costs the Teacher one extra tap per export (picking the
  file) compared to the stored-template flow grades export will offer.

### Consequences

- `AttendanceMonth`'s `canExport` reflects whether the month has any recorded
  day (`recordedDayCount > 0`), not whether a template is stored — there is no
  stored template for this feature. The combined `Export` screen (ADR-pending,
  ticket #14) mockup already shows the attendance card without an
  "unavailable" variant, which matches.
- `ExportMonthlyAttendanceUseCase` takes the picked `templateUri` as a
  parameter on every call; it does not read `SiagieImportStore`.
- If a later ticket wants the stored-template flow for attendance too (to
  drop the per-export picker), it needs a schema change to key
  `imported_template` by month, which is out of scope here and was flagged to
  the dispatcher before implementation per the wave's constraints.

## Screen and share sheet placement

`AttendanceMonthRoute` (`feature:attendance`) owns both the SAF document
picker (`ActivityResultContracts.OpenDocument`) and the share sheet
(`Intent.ACTION_SEND` through a `FileProvider`), following the precedent in
`feature:backup`'s `BackupRoute` rather than routing either through `:app`.
Every feature module using `gema.android.feature` already carries its own
manifest and resources, and `BackupRoute` already owns its picker and its
share extension function; splitting attendance's picker and share sheet into
`:app` would be new precedent, not consistency. `core/CLAUDE.md` says the code
wins when a doc and the code disagree — here the peer instruction is not
wrong, just less specific than what the codebase already does.
