---
status: accepted
---
# One export produces exactly one Period for one Section

A grades export writes exactly one Period — the one selected on the Export
screen — for one Section. This follows the SIAGIE template itself: per the
RVM 094-2020 instructive ("Registro de calificaciones en SIAGIE por
periodo"), primaria registers AD/A/B/C by period, grade and section, and the
template a Teacher downloads is already scoped that way. Exporting every
Period of a school year into one file would produce a file SIAGIE cannot
import.

## Considered Options

- **Export every Period of the active School Year in one file.** Rejected:
  the SIAGIE template's own structure is per period per section, so a
  multi-period file has no matching import target.

## Consequences

- The Export screen ("Entregar") always asks the Teacher to confirm a single
  Period and Section before generating a file; there is no "export
  everything" action.
- Blockers (students without a level, a `C` without a Descriptive Conclusion,
  competencies not marked as worked) are scoped to that one Period and
  Section, and are surfaced as tap-through rows inside the grades card rather
  than a separate summary across periods.
- A Teacher who teaches several Sections, or wants several Periods filed,
  repeats the export once per Period-Section pair — this is the SIAGIE
  workflow's own granularity, not an app limitation to remove later.
