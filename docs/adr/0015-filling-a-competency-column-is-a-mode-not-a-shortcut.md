---
status: accepted
---
# Filling a competency column is a mode, not a shortcut

Closing a Period means deciding one Period Level per Student per Worked
Competency. A Section of thirty Students and three Worked Competencies is ninety
decisions. Reaching each one through the PeriodLevelSheet costs a tap to open,
a tap to pick and a tap to dismiss: two hundred and seventy taps.

Tapping a competency header therefore enters **fill column mode**. A picker
holding AD / A / B / C / no level appears at the bottom of the grid, bound to one
Student at a time. Each pick records that level and advances to the next
Student, so a column costs one tap per Student. *Listo* leaves the mode early,
and so does picking a level for the last Student. Tapping a cell also opens its
sheet, which stays the only place that holds the Unworked Comment and the
Descriptive Conclusion.

(#170, PR #183: the mode shipped with no way to advance a Student without
recording a level, so there never was a *Saltar*. This paragraph originally
described one; it is corrected here rather than superseded, since the mode
itself did not change.)

The mode never computes or suggests anything: every level is still a tap the
Teacher makes, one Student at a time, so ADR 0003 holds. What it removes is the
navigation between decisions, not the decision.

The rejected alternative was a long-press "apply to the whole column", which is
faster still and wrong: it writes a level for Students the Teacher never looked
at, and an accidental one is invisible in a grid of letters.
