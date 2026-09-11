---
status: accepted
---
# An Activity derives its Period from its date, never a picker

`ActivityForm` takes a name, a date and a multi-select of Competencies. The
Period is never a field the Teacher fills: `SaveActivityUseCase` resolves it
by finding, among the Section's School Year Periods, the one whose range
contains the date (`FindPeriodForDateUseCase`). Changing the date to a day
inside another Period moves the Activity there on the next save, and the form
states that before it happens.

The Competency multi-select is restricted to the Worked Competencies of the
Period the current date resolves to (`GetWorkedCompetenciesUseCase`), across
every active Area. A date change reloads that list and drops any selection the
new Period does not offer, because a Competency unworked in the destination
Period cannot produce an Evidence Level there.

## Considered Options

- **Let the Teacher pick the Period directly.** Rejected: it duplicates a
  derivation SIAGIE already makes from the calendar, and a School Year's
  Periods already cover it without gaps (ADR 0013). A second, independent
  picker could disagree with the date and nobody would notice until export.
- **Freeze the Period at creation, ignore later date edits.** Rejected: an
  Activity dated into the wrong Period by a typo would stay there forever,
  and `GetActivityEvidenceStudentCountsUseCase` would keep counting it against
  a Period it no longer belongs to by date.

## Consequences

- Deleting or editing an Activity's name, date or Competencies never touches
  `period_level`: `DeleteActivityUseCase` only reaches `activity` and
  `evidence_level`, and `SaveActivityUseCase` only writes `activity` and
  `activity_competency`. A Period Level is a decision the Teacher makes
  separately at Period close (ADR 0003); an Activity edit cannot silently
  change it.
- An Evidence Level is optional per Student and Competency of the Activity:
  `RecordEvidenceLevelUseCase` deletes the row on a null level, mirroring
  `SavePeriodLevelUseCase`, so "no level" is a first-class outcome rather than
  a row the Teacher forgot to fill.
- The Period Level sheet shows every Evidence Level recorded for that Student
  and Competency in the Period, ordered by Activity date
  (`GetPeriodLevelSheetContextUseCase`). The list is read-only: nothing on the
  sheet copies an Evidence Level into the Period Level, keeping that decision
  the Teacher's alone (ADR 0003).
