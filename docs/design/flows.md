# Gema MVP navigation map

Scope: the v1 spec (issue #1) and tickets #4-#14. Everything here is offline,
single device, no account. Spanish is the shipped interface language; this
document uses the English glossary from `CONTEXT.md` so identifiers, screen
names and test names map one to one.

## Design constraints that shape every flow

The Teacher is standing in front of thirty children, holding a low-end phone in
one hand, in a room with no signal and often no power outlet. Every decision
below follows from that.

| Constraint | What it forbids | What it forces |
|---|---|---|
| No network, ever | Spinners, retries, pull-to-refresh, offline banners, sync status, conflict dialogs | Reads resolve from `GemaDb` fast enough to render the first frame with data; a loading state is a one-frame flash, never a designed screen |
| Writes must survive a dead battery | A "Save" button on high-frequency screens (Attendance, Evidence Levels, Period Levels) | Every tap on a level or a status is its own persisted write; the screen never holds a dirty buffer |
| One-hand use while standing | Top-right primary actions, swipe-only actions, drag and drop | The primary action sits in a bottom bar or a FAB inside the thumb arc |
| Small, low-density screens (360dp, 4.5"-5.5") | Real tables, side-by-side panes, more than four tabs | The Period Levels grid is a pinned Student column plus a horizontally scrolled competency band; everything else is one column |
| Low-end CPU and RAM | Transitions over 200ms, image-heavy empty states, recomposition over 200-row grids | Lazy lists with stable keys, no cross-fade page transitions |
| Gloves, chalk dust, sunlight | 32dp targets, colour as the only signal | 48dp minimum touch target (already the `core/ui` rule); a level chip carries the letter, not only a colour |
| Data loss is unrecoverable | Silent deletes, destructive defaults | Withdrawal instead of deletion, confirmation for anything that destroys records, a Backup reminder |

Two product rules matter as much as the visual ones:

- **Nothing is ever computed.** No screen shows a suggested, averaged or
  predicted Achievement Level (ADR 0003). Evidence Levels are shown *next to*
  the Period Level field, read-only, and never pre-fill it.
- **Import and Export are previewed before they act.** The Teacher always sees
  what will change — created / updated / withdrawn, or the list of blocking
  gaps — before a single byte is written.

## Top-level map

```
                        +-----------------+
   first launch  ------>|  SetupYear      |  year dates + bimester/trimester
   (no School Year)     +--------+--------+
                                 v
                        +-----------------+
                        |  SetupSection   |  Grade + name of the first Section
                        +--------+--------+
                                 v
   later launches ----->+-----------------+
                        |      Home       |<--------------+
                        +--------+--------+               |
       +--------------+----------+----------+-------------+
       v              v          v          v             |
  +---------+   +----------+ +-------+ +---------+        |
  |SchoolYrs|   |SectionFrm| |Backup | | Section |--------+
  +----+----+   +----------+ +-------+ | Detail  |
       v                               +----+----+
  +---------+                              |
  | Periods |   +--------------+-----------+--------+--------------+
  +---------+   v              v           v        v              v
        +------------+ +------------+ +----------+ +--------+ +----------+
        |  Students  | |AttendanceD | |PeriodLvls| |Activit.| |  Export  |
        +-----+------+ +-----+------+ +----+-----+ +---+----+ +----+-----+
              |              |             |           |
        +-----+------+       v             v           v
        v            v +------------+ +----------+ +---------+
  +-----------+ +----------+        | |WorkedComp| |ActivityF|
  |StudentForm| |ImportPrev|  Month | +----------+ +----+----+
  +-----------+ +----------+--------+                   v
                                              +--------------+
                                              |ActivityEvid  |
                                              +--------------+

  SectionDetail also reaches SectionAreas (hide/unhide Areas) and
  SectionForm (rename). Home's Section card also jumps straight into
  AttendanceD for today. PeriodLevelSheet is a bottom sheet over
  PeriodLvls; Export's blocking gaps list inline in its own card instead
  of a separate screen.
```

There is **no bottom navigation bar**. The Section is the working context for
almost every task, so the hierarchy is Home -> Section -> task. A bottom bar
would have to either drop the Section context or repeat it in five tabs.

## 1. First-run setup (US 1-5, 8, 64 — ticket #4)

Entry point: cold start with zero School Years in `GemaDb`. No onboarding
carousel, no permission request, no account — the Teacher lands on a form.

```
SetupYear ---> SetupSection ---> Home
    |               |
   back exits      back returns to SetupYear (nothing committed yet)
```

**SetupYear** collects the year label (pre-filled from the device date), start
and end date, and the Period kind (bimester = 4 Periods, trimester = 3).
Periods render as a compact list — one row per Period, its dates computed by
dividing the year range evenly — with a short reassurance line at the top
explaining that the dates are computed automatically. Tapping a Period row
opens a small editor for that one Period instead of exposing eight raw date
fields on the main screen. Primary action: *Continue*.

**SetupSection** collects Grade (1-6) and Section name. Every primary Area starts
active; the screen says so and offers "I do not teach every Area" as a secondary
link to `SectionAreas`, so the default path is one tap. Primary action: *Finish*.
The School Year, its Periods and the first Section are persisted here in one
transaction — abandoning setup halfway leaves no orphan year.

Offline notes: dates use a plain numeric picker, not a network-backed calendar.
The Period division is arithmetic, so the screen works with a wrong device clock;
any Period date is correctable later from `Periods`.

**Later years.** `SchoolYears` lists every School Year with the active one
marked, creates a new one through the same two-step form, and switches the active
year without deleting anything (US 6, 7). `Periods` edits Period dates after the
fact (US 4) and marks the Period containing today (US 5). That marker is the
single source of "current Period" for every other screen, so no screen asks the
Teacher to choose a Period by hand except to look at a past one.

## 2. Home and the Section hub

**Home** shows the active School Year, the current Period with days remaining,
the Backup reminder banner when the last Backup is older than the configured
threshold (US 59), and the list of Sections. Primary action: a FAB that adds a
Section. Tapping a Section's title opens `SectionDetail`; each card also
carries an inline *Take attendance today* button that jumps straight into
`AttendanceDay` for that Section in one tap, without the detour through the
hub.

**SectionDetail** is a hub, not a dashboard. It shows Grade and name, the Student
count, whether a SIAGIE Template is stored, and today's attendance state ("not
taken" / "28 of 30 present"). It offers exactly six destinations — Students,
Attendance, Period Levels, Activities, Export, and an overflow with Rename and
Areas. The primary action is *Take attendance today*, because that is the daily
job.

Deleting a Section happens from `SectionForm`, not from this hub: with Students
or records it confirms with what will be lost (US 14); an empty Section deletes
with a plain confirm (US 13).

## 3. Students (US 15, 20-24 — ticket #6)

```
SectionDetail ---> Students ---+---> StudentForm   (add / edit / withdraw)
                               +---> ImportPreview (SIAGIE template)
                               +---> reactivate    (inline confirm)
```

**Students** lists active Students ordered by surname, matching the paper
register and the SIAGIE Template. Withdrawn Students are collapsed under a
"Withdrawn (n)" footer rather than hidden, so reactivation stays discoverable
(US 21). A search field appears only when the list exceeds one screen. Primary
action: FAB *Add Student*; the overflow carries *Import from SIAGIE*.

**StudentForm** takes Student Code (14 digits, validated inline and for
uniqueness within the Section) and full name. Withdrawal is a dated action on an
existing Student, never a delete.

Offline note: the Student Code field uses a numeric keyboard with a visible digit
counter, because a wrong code silently breaks Export months later.

## 4. SIAGIE import (US 16-19, 24 — ticket #10)

```
Students ---> [Android document picker] ---> ImportPreview ---> apply ---> Students
                                                  |
                                                  +-- reject: file is not this Section
```

The picker is the system document picker; the app ships no file browser.
`ImportPreview` never writes. It shows three counts — *will be created*, *will be
updated*, *will be proposed as Withdrawn* — each expandable into names, plus the
resolved file name. The Teacher confirms with *Apply import* or cancels. Missing
Students are proposed as Withdrawn with a per-name checkbox defaulted on, so a
partial roster is not an all-or-nothing decision (US 19).

A file whose Grade or Section does not match is rejected on the preview screen
with the two values compared, not a generic error (US 24).

On apply, the original file bytes and name are stored against the Section. That
is what makes SIAGIE Export possible later (ADR 0002); `SectionDetail` and
`Export` both read that flag.

## 5. Daily attendance (US 25-31 — ticket #8)

```
SectionDetail ---> AttendanceDay(today) <--- date stepper ---> AttendanceDay(past)
                          |
                          +---> AttendanceMonth (summary + SIAGIE attendance export)
```

**AttendanceDay** is the highest-frequency screen in the app and the one most
worth optimising. Every active Student is a row with a four-state toggle:
present / late / absent / justified. Every Student renders as present and
**nothing is persisted until the first tap** on that Student, so opening a date
to look at it does not fabricate records. Each tap writes one row immediately
(US 31) — no save button, no confirmation dialog, no undo snackbar that could be
lost with the process. A header action, *Todos presentes*, records present for
every Student that is still unmarked in one tap, for the common case where the
whole Section showed up. A subtitle under the date stepper states that every
tap saves automatically, so the Teacher never looks for a save button.

Unmarked rows render visibly distinct — a warm surface colour plus a dashed
outline, never colour alone — and a header counter reads "N sin marcar" so the
Teacher can see at a glance who is left before reaching for *Todos presentes*
or tapping through the rest by hand.

The date changes with a left/right stepper plus a tappable label opening a
picker. Future dates are unreachable: the forward control disables at today.
Withdrawn Students disappear from dates on or after their withdrawal date
(US 29).

Offline and low-end notes: the row is a single `GAttendanceToggle`, four segments
of at least 48dp, no nested scrolling and no swipe gesture. A header pill shows
the running present/total count so the Teacher can check against a head count
without scrolling back.

**AttendanceMonth** (ticket #13) shows one row per Student with counts per status
for the selected month and offers the SIAGIE attendance export.

## 6. Competencies and the Period Levels grid (US 32-33, 39-46 — tickets #7, #9)

```
SectionDetail ---> PeriodLevels (Area picker) ---+---> WorkedCompetencies
                                                 +---> PeriodLevelSheet (cell)
                                                 +---> Missing filter
```

**WorkedCompetencies** is the gate. For the current Period and a chosen Area it
lists the seeded CNEB Competencies with a checkbox each (US 33). Nothing is
typed; the seed ships in the app. Only checked Competencies become grid columns
and Export cells. Opening `PeriodLevels` for an Area with no Worked Competencies
shows an empty state that routes here in one tap.

**PeriodLevels** is the grid: Students down, Worked Competencies across. On a
360dp screen a real table is unreadable, so the layout is a **pinned Student
column plus a horizontally scrolled band of competency cells**, each cell a
`GLevelChip` showing AD / A / B / C, an Unworked Comment marker, or an empty
slot. A row of SIAGIE ordinals (01, 02, 05) sits above the band so the Teacher
always knows which competency a column is.

Tapping a column's header instead of a single cell enters **column mode**: a
bottom picker offers AD / A / B / C / Sin nivel for the current Student only,
records the pick and advances to the next Student automatically, so grading
one competency for the whole Section is a straight run of taps instead of
re-aiming at a cell each time. *Listo*, or picking a level for the last
Student, exits column mode.

Tapping a single cell, outside column mode, still opens **PeriodLevelSheet**,
a bottom sheet holding everything for one Student x one Competency:

- the four Achievement Levels as one picker row;
- the three Unworked Comments as a second, mutually exclusive group — a Period
  Level holds exactly one of the two (ADR 0003, ticket #9);
- the Descriptive Conclusion field, marked required-for-SIAGIE when the level is
  C but **never blocking**: saving a C without one succeeds and the cell is
  flagged incomplete (US 44);
- a read-only list of every Evidence Level recorded for that Student and
  Competency in the Period, with Activity name and date (US 39, 41).

That list is facts, nothing more. There is no "apply suggestion" button and no
computed value anywhere on the sheet.

A *Missing* toggle in the top bar filters the grid to empty cells and shows the
count (US 46). That is how a Teacher closes a Period.

## 7. Activities and Evidence Levels (US 34-38 — ticket #11)

```
SectionDetail ---> Activities ---+---> ActivityForm     (name, date, Competencies)
                                 +---> ActivityEvidence (Students x Competencies)
```

**Activities** lists the Activities of the current Period, newest first, each
showing its date and the Competencies it touches. **ActivityForm** takes a name,
a date and a multi-select of Competencies drawn from the Worked Competencies of
the Period the date falls into — the Period is derived from the date and never
picked (US 35). Changing the date can move the Activity to another Period; the
form states that before saving.

**ActivityEvidence** records one Evidence Level per Student per Competency of
that Activity. With one Competency it is a flat list of Students, each with a
level picker; with several it reuses the pinned-column grid from Period Levels.
Leaving a Student empty is a valid, expected outcome (US 37) — "no level" is a
first-class option in the picker, not the absence of an action.

Deleting an Activity deletes its Evidence Levels and touches no Period Level.

## 8. Export, i.e. "Entregar" (US 47-54 — tickets #12, #13, #14)

```
SectionDetail ---> Entregar ---+-- SIAGIE grades -----> validate --+-- ok ---> share sheet
                               |                                   +-- gaps -> inline rows
                               +-- SIAGIE attendance (month) -----------------> share sheet
                               +-- PDF / CSV summary --------------------------> share sheet
```

One **Entregar** screen (screen id `Export`) holds all three outputs for the
selected Period, because a Teacher thinks "I have to hand in the period", not
"I need an xlsx". The title reads *Entregar*, matching that goal rather than
the file format.

SIAGIE grades export is offered only when a Template is stored for the Section.
Without one, that option is visibly disabled with the reason, and the PDF/CSV
option is promoted (US 53) — the app explains, it does not just hide.

Validation runs in the domain before any file is produced. If any C lacks a
Descriptive Conclusion, the grades card lists every offending
Student x Competency as a tap-through row inside the card itself — no separate
bottom sheet — each one opening the `PeriodLevelSheet` that fixes it and
returning to `Entregar`. *Generar archivo* enables only once nothing is
pending. No partial file is ever written (US 49).

On success the file keeps its original SIAGIE name and goes to the Android share
sheet (US 48, 51). Gema owns no "sent" state, does not rename, does not
re-encode and keeps no copy — the share sheet is the end of the flow, and
WhatsApp, Bluetooth or a USB file manager is the transport.

## 9. Backup and restore (US 55-59 — ticket #5)

```
Home ---> Backup ---+-- Create backup ----> share sheet
                    +-- Restore ---> [picker] ---> confirm replacement ---> restart
                    +-- Reminder threshold (days)
```

**Backup** states the last Backup in plain language ("12 days ago"), creates a
`.gema` file and hands it to the share sheet, and restores one after a
confirmation that names what will be replaced (US 58). Restore restarts the
process; the screen warns before it happens, because a self-restarting app
otherwise reads as a crash.

The reminder threshold is a number of days, edited here, and drives the Home
banner. That banner is the only proactive nag in the app.

## Cross-cutting behaviours

- **No global loading screen.** Every screen renders its first frame from the
  database. `isLoading` exists in `UiState` but is expected to be true for a
  single frame; no screen is designed around it.
- **Errors are local and actionable.** There is no generic error screen. An
  import mismatch, an invalid Student Code and an export gap each explain the
  specific problem on the screen that can fix it.
- **Back always means back.** No screen traps the Teacher. Forms with unsaved
  edits confirm on back; high-frequency screens have nothing unsaved to lose.
- **Confirmation is reserved for destruction.** Deleting a Section with records,
  restoring a Backup, and applying an import that withdraws Students. Nothing
  else asks twice.

## Open questions

Collected at the end of `screens.md`, together with the per-screen questions.
