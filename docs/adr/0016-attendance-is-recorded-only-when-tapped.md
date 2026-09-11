---
status: accepted
---
# An Attendance row exists only where the Teacher tapped

The `attendance` table holds one row per Student per date, written the moment
the Teacher taps a status. Opening a date writes nothing: `GetAttendanceDayUseCase`
joins the Section's Students with whatever rows exist for that date and fills
the gaps with `AttendanceStatus.PRESENT` and `isRecorded = false`. Absence of a
row is the domain's way of saying "nobody has decided yet", and the screen says
so with a warm surface, a dashed toggle outline and an "N sin marcar" counter.

## Considered Options

- **Materialise a full day on open.** Rejected: a Teacher who opens yesterday to
  read it would silently create thirty "present" records, which is a claim about
  a day nobody reviewed. It also makes "the day was never taken" unrepresentable,
  so the Section hub and the Home cards could not tell *Sin tomar* from a day
  where everyone really was present.
- **A nullable status instead of a separate flag.** Rejected: `GAttendanceToggle`
  would then accept a row with no status at all, which is never a correct render.
  The default lives in the use case, the flag says whether it is stored yet.

## Consequences

- A monthly summary (#13) counts an unmarked day as no data, not as presence;
  the SIAGIE attendance export must decide explicitly what an unmarked school
  day means before it writes a cell.
- *Todos presentes* exists precisely because the default is not persisted: it
  turns the twenty-eight untouched rows into records in one tap, and leaves
  every Student the Teacher already marked untouched.
- Withdrawal hides a Student from dates on or after the withdrawal date while
  the rows written before it stay readable, since they are independent rows and
  not slots inside a day object.
- A future "school day" calendar can be added without a data migration: today
  the row set already distinguishes recorded days from every other date.
