---
status: accepted
---
# The Period Levels summary reuses the grid model and renders its PDF in app

A Section without a stored SIAGIE template has no grades export at all (ADR
0002 only fills an imported template). Ticket #14 gives it one: a PDF or CSV
of every Period Level, one table per active Area with a Worked Competency —
one row per Student, one column per Worked Competency of that Area — not one
flat table spanning every Area. Per-Area tables also bound how many columns
a PDF page has to fit; a section teaching every CNEB Area at once would
otherwise squeeze dozens of competencies into one page.

`GetPeriodLevelSummaryUseCase` returns `PeriodLevelSummary`, a list of
`PeriodLevelAreaSummary(area, grid: PeriodLevelGrid)` — one `PeriodLevelGrid`
per Area, reusing that existing model instead of inventing a new row/column
shape. It follows `GetMissingPeriodLevelCountUseCase`'s shape — read the
section's hidden Areas, the Worked Competencies for every Area, active
Students and recorded levels — filtering per Area and dropping any Area with
no Worked Competency (it would otherwise render as an empty table). A
competency the Teacher never marked worked never becomes a column, in either
format.

CSV rendering (`PeriodLevelSummaryCsv`) is a hand-written, JVM-only function
in `core:domain`: no serialization library, and no need for one to escape a
handful of columns per RFC4180. Each Area is an title row, its header row,
then its rows, separated by a blank line from the next Area. A recorded `C`
carries its Descriptive Conclusion in the same cell (`"C: <conclusion>"`)
rather than dropping it — a Descriptive Conclusion is exactly what a blocked
grades export (ADR 0018) is missing, so the one export this Section has must
not hide it either. The file opens with a UTF-8 BOM and uses CRLF line
endings, because Excel on Windows — where a Peruvian school actually opens
this file — renders accented Spanish as garbage without the BOM. It is
exercised directly by unit tests.

PDF rendering cannot stay JVM-only: laying out a readable A4 table needs
`android.graphics.pdf.PdfDocument`, `Canvas` and `Paint`. `core:domain`
declares `PeriodLevelSummaryPdfRenderer` as the seam; the implementation
using `PdfDocument` lives in `app`, not `core:database`. `core:database` is
the `GemaDb` persistence layer (schema, repositories, the SQLDelight
drivers); a PDF renderer is presentation-adjacent Android framework code
with no row to persist, so it does not belong there. `app` is already the
Android layer for Koin wiring and this is exactly that kind of seam
implementation, mirroring how `BackupDocuments` and `SiagieDocuments` sit in
`core:database` because they *do* read and write through content
resolvers tied to stored data — the file writer here (`CacheDirSummaryDocuments`)
follows that same shape and reuses the `exports` cache directory and
`FileProvider` ticket #12 already wired into `feature:export`, instead of
adding a second one.

`android.graphics.pdf.PdfDocument` cannot construct under Robolectric in
this toolchain: `PdfDocument()` reports itself closed on the very first
`startPage()` call, with or without `@GraphicsMode(NATIVE)` or a pinned
`@Config(sdk = ...)`. The PDF smoke test required by the ticket
("PDF is verified by a smoke test that the file is produced") therefore
runs as an instrumented `androidTest` in `app`, not a JVM unit test; it is
outside `testDebugUnitTest` and needs a device or emulator
(`./gradlew connectedDebugAndroidTest`) to execute. **It does not run in
CI and is not part of the pre-commit gate**; nothing in this repo runs
`connectedDebugAndroidTest` today. Everything else -- the summary grid,
the CSV writer (unit-tested directly, no device needed), the file-name
slug, the cache-dir file writer, and the ViewModel wiring -- is covered by
ordinary JVM unit tests under `testDebugUnitTest`.

The summary lives on the existing "Entregar" screen (ticket #12) as an
additive "Resumen para imprimir" card with PDF and CSV buttons, reusing
that screen's period picker, share sheet and `ExportMessage.EXPORT_FAILED`.
`ExportUiState` tracks the in-flight export as one `activeExport:
ActiveExport?` (`GRADES` / `SUMMARY_CSV` / `SUMMARY_PDF` / `null`) rather
than three independent booleans, so "at most one export runs at a time" is
structural rather than a convention three flags could drift out of.
