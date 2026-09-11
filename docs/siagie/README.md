# SIAGIE reference material

Official Minedu instructives describing how grades and attendance are loaded
into SIAGIE through Excel templates. Gathered 2026-09-10 from public UGEL and
SIAGIE sources. The real template is generated per teacher inside SIAGIE
(login required), so the exact `.xlsx` is not available here yet.

## Files

- `registro-por-periodo-2023-rvm-094-2020.pdf` — grading rules per level
  (RVM 094-2020-MINEDU), primary uses literal scale AD/A/B/C per competency per period.
- `ugel-san-marcos-plantilla-excel-primaria.pdf` — 48 pages, page 18 shows the
  primary grades template; pages 26-30 show the upload flow
  (Evaluacion -> Registro de calificaciones -> Notas Finales -> Por Excel).
- `guia-rapida-v3.3.0-asistencia-excel.pdf` — monthly attendance template flow.
- `plantilla-primaria-captura.png` — screenshot of the primary grades template.

## Grades template (primary) — observed structure

File name example: `6 Primaria EBR.xlsx`. One sheet per curricular area
(tabs seen: CAST SEGNL, CIENC TEC, COMU, EFIS, EREL, INGLES EXT, MATE, PPSS, GEST AU, ...).

Header rows 1-3, data from row 4. Columns:

| Col | Header            | Notes                                              |
|-----|-------------------|----------------------------------------------------|
| A   | ID                | SIAGIE internal student id                         |
| B   | CodEstudiante     | 14-digit student code                              |
| C   | Nombres           | full name, uppercase, surnames first               |
| D   | Competencia 01 NL | dropdown: AD, A, B, C, Comentario 1/2/3            |
| E   | Conclusion descriptiva | free text, optional unless NL is C            |
| F.. | Competencia 02 NL, Conclusion descriptiva, ... repeat per competency |

Legend block below the data lists competencies by number for that area
(e.g. PPSS: 01 Construye su identidad, 02 Convive y participa ..., 05 Gestiona
responsablemente los recursos economicos).

NL dropdown values (exact Spanish strings from the UGEL San Marcos instructive legend; whether the cell stores the short token or the full sentence is unconfirmed):

- `Comentario 1. No se logró realizar acciones para su desarrollo`
- `Comentario 2. No se cuenta con evidencia suficiente para determinar nivel de logro.`
- `Comentario 3. Otro.`

Meaning:
- `AD`, `A`, `B`, `C` — achievement levels.
- `Comentario 1` — no actions were carried out for its development.
- `Comentario 2` — not enough evidence to determine the level.
- `Comentario 3` — other.

Rules:
- Only competencies actually worked in the period are filled.
- If NL is `C`, the descriptive conclusion is mandatory; otherwise optional.
- Grades are registered per period (bimestre or trimestre, set by the school).
- The downloaded file name must not be changed before upload.

## Attendance template (monthly)

File name pattern: `AsistenciaIE_<ie>_<grado>_<seccion>.xls`. The instructive
names the extension `.xls`; our reader and writer only speak the OPC/`.xlsx`
zip format (ADR 0005), so the fixture below uses `.xlsx` and this is a second
open question alongside the codes.

Sheet 1 `Generalidades` (school and section info, must not be edited).
Sheet 2: one row per student, one column per day of the month.

**This structure is modelled from the instructive, not observed in a real
generated file** — same status as the grades template (see above). The fixture
under `core/siagie/src/test/resources/AsistenciaIE_12345_6_A.xlsx`, generated
by `core/siagie/tools/generate_attendance_fixture.py`, mirrors the grades
fixture's layout: header rows 1-3, data from row 4, columns A `ID`,
B `CodEstudiante`, C `Nombres`, then one column per day starting at D.

### Attendance status codes — PENDING CONFIRMATION AGAINST A REAL FILE

We do not have a real SIAGIE-generated attendance file. The codes below are our
own choice, kept in one place (`AttendanceSiagieCode` in `core:domain`, read by
both `core:siagie` and `feature:attendance` so nothing repeats it) so a
correction is a one-line edit once a real file is available:

| App status (`AttendanceStatus`) | SIAGIE code written |
|----------------------------------|----------------------|
| `PRESENT`                        | `P`                  |
| `LATE`                           | `T`                  |
| `ABSENT`                         | `F`                  |
| `JUSTIFIED`                      | `FJ`                 |

An attendance day the Teacher never marked (ADR 0016) writes nothing: the
export leaves that day's cell exactly as the template had it, because an
absent row is not a claim about what happened.

## Pending

- Obtain a real generated `.xlsx` (or confirm `.xls`) for a primary section's
  grades and attendance templates (needs a teacher's SIAGIE account).
- Confirm exact attendance codes in the table above against that file.
- Confirm the hidden ID column mapping so exports can round-trip.
- Confirm the attendance sheet names, the sheet order, and the day-column
  headers (numbers vs. dates) against a real file.
