# Gema MVP screens

One section per screen. Each carries an ASCII wireframe at roughly 360dp width,
the `UiState` it needs with explicit types, and the intents it emits.

Conventions, from `.claude/rules/architecture.md` and `naming.md`:

- `<Feature>UiState` is a `data class`, every field `val`, no second source of
  screen state.
- `<Feature>UiIntent` is a `sealed interface`; names say what the Teacher did.
- `<Feature>UiEffect` is a `sealed interface`; one-shot, never stored in state.
- The screen is stateless and receives `state` plus `onIntent: (UiIntent) -> Unit`.
- Presentation rows (`StudentRow`, `LevelCell`, ...) are presentation models
  living beside the feature. A domain model never reaches a `UiState` directly.

Types referenced below and owned by `domain`: `SchoolYearId`, `PeriodId`,
`SectionId`, `StudentId`, `Area`, `CompetencyId`, `ActivityId`, `Grade`,
`PeriodKind`, `AchievementLevel`, `UnworkedComment`, `AttendanceStatus`.
Dates are `kotlinx.datetime.LocalDate` / `YearMonth`.

`isLoading: Boolean` appears on every state because the MVI template defines it;
on this app it is true for roughly one frame, so no wireframe draws it.

## Mockups

Some screens carry a link to a rendered HTML mockup under `mockups/`. That
mockup html carries the exact spacing, sizes and colors; the ASCII wireframe
below it is only the structure. When they disagree, the mockup wins.

`mockups/alternative-b-attendance-grid.html` and
`mockups/alternative-c-levels-per-student.html` are low-fidelity exploration
artboards for the attendance and levels flows. They do not map to a numbered
screen below and are not the accepted proposal.

---

## 1. SetupYear

Mockup: [html](mockups/setup-year.html) · period editor
[html](mockups/setup-year-period-editor.html) (no PNG until Phase 4
regenerates them from the emulator).

First screen of a cold start with no School Year. Entry: app launch.
Shows the year, its kind and its Periods. Primary action: *Continuar*.

Registro layout: no top bar. A `GStepHeader` opens the screen with the
"PASO 1 DE 2" eyebrow (`labelSmall`, `onSurfaceVariant`), the title
(`titleMedium`) and one line of description (`bodyLarge`, default color).
Below it, `GTextField` for the year, then the two
`GDateField`s side by side, then the "¿CÓMO EVALÚA TU ESCUELA?" eyebrow over a
two-segment `GSegmentedPicker` whose selected segment uses the default
`primaryContainer` fill. The Periods group is an eyebrow
followed by read-only `GListItem` rows on hairlines: a 28dp tabular leading
ordinal (I, II, III, IV), the range as `bodyLarge` ("1 mar – 15 may"), a
chevron. Nothing on this screen edits a Period inline; the list only opens the
editor. *Continuar* is the `bottomAction` PRIMARY `GButton`.

```
+------------------------------------------+
|  PASO 1 DE 2                             |
|  Tu año escolar                          |
|  Calculamos las fechas de los periodos   |
|  por ti. Puedes ajustar cualquiera.      |
|                                          |
|  +------------------------------------+  |
|  | Año                                |  |
|  | 2026                               |  |
|  +------------------------------------+  |
|  +---------------+  +----------------+   |
|  | Inicio    [c] |  | Fin        [c] |   |
|  | 01/03/2026    |  | 20/12/2026     |   |
|  +---------------+  +----------------+   |
|                                          |
|  ¿CÓMO EVALÚA TU ESCUELA?                |
|  +------------------+-----------------+  |
|  |  Bimestres (4)   |  Trimestres (3) |  |
|  +------------------+-----------------+  |
|                                          |
|  PERIODOS                                |
|  ----------------------------------------|
|  I    1 mar – 15 may                   > |
|  ----------------------------------------|
|  II   18 may – 31 jul                  > |
|  ----------------------------------------|
|  III  10 ago – 16 oct                  > |
|  ----------------------------------------|
|  IV   19 oct – 20 dic                  > |
+------------------------------------------+
|            [    Continuar    ]           |
+------------------------------------------+
```

Tapping a Period row opens a `GDialog` over this screen. Its title is the
Period label; `GDialog` has no subtitle param, so "Solo cambia este periodo"
renders as content-slot text above the two `GDateField`s, which sit side by
side (they reflow to two lines at font scale
1.3, see `components.md`). `GDialog`'s dismiss button is a TEXT `GButton`
("Cancelar"); the confirm is a filled PRIMARY `GButton` ("Guardar"), never a
text button — `GDialog.kt` renders `confirmButton` as `GButton(variant =
GButtonVariant.PRIMARY)` unless `isDestructive` is set.

```
+------------------------------------------+
|  II Bimestre                             |
|  Solo cambia este periodo                |
+------------------------------------------+
|  +---------------+  +----------------+   |
|  | Inicio    [c] |  | Fin        [c] |   |
|  | 18/05/2026    |  | 31/07/2026     |   |
|  +---------------+  +----------------+   |
|                                          |
|                    Cancelar   [ Guardar ]|
+------------------------------------------+
```

```kotlin
data class SetupYearUiState(
    val isLoading: Boolean = false,
    val yearLabel: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val periodKind: PeriodKind = PeriodKind.BIMESTER,
    val periods: List<PeriodDraftRow> = emptyList(),
    val editor: PeriodEditorState? = null,
    val yearLabelError: SetupYearMessage? = null,
    val dateRangeError: SetupYearMessage? = null,
    val canContinue: Boolean = false,
)

data class PeriodDraftRow(
    val ordinal: Int,
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val error: PeriodRangeError?,
)

data class PeriodEditorState(
    val ordinal: Int,
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val error: PeriodRangeError?,
)
```

Intents: `YearLabelChanged(value: String)`, `StartDateChanged(value: LocalDate)`,
`EndDateChanged(value: LocalDate)`, `PeriodKindSelected(kind: PeriodKind)`,
`PeriodClicked(ordinal: Int)`, `EditorStartDateChanged(value: LocalDate)`,
`EditorEndDateChanged(value: LocalDate)`, `EditorConfirmed`, `EditorDismissed`,
`ContinueClicked`.

Effects: `NavigateToSetupSection(draft: SchoolYearDraft)`.

Note: no top bar and no in-screen back control — the system back gesture pops
the nav back stack directly. From a cold start SetupYear is the start
destination with no back stack to pop; reached from SchoolYears' "Nuevo año"
it pops back there like any other screen.

Note: nothing is persisted here. The draft travels to step 2 and both are
written in one transaction, so an abandoned setup leaves no orphan year.

Note: the whole year is prefilled from the device date on first render — the
label is that year, the range is 1 March to 20 December of it — so the Teacher
can accept it with zero typing; every field stays editable for a wrong device
clock or a school that starts elsewhere. The eight raw Period date fields that
used to sit inline are gone — every `PeriodDraftRow` date is computed by
dividing `startDate..endDate` evenly, shown as a compact list, and corrected one
Period at a time through `editor`. Changing the range or the kind recomputes
every Period and discards those corrections. See ADR 0013.

Copy changes (for the implementation ticket)

- `setup_year_helper_text`: "Solo lo que necesitamos para empezar. Todo se
  puede corregir después." -> "Calculamos las fechas de los periodos por ti.
  Puedes ajustar cualquiera." (explains the new auto-computed Period dates,
  ADR 0013).

---

## 2. SetupSection

Mockup: [html](mockups/setup-section.html) (no PNG until Phase 4
regenerates it from the emulator).

Entry: from SetupYear, or from `SchoolYears` when creating a later year.
Shows Grade, Section name and the Area default. Primary action: *Terminar*.

Registro layout: the same `GStepHeader` as step 1, "PASO 2 DE 2", with a
one-line description ("Como figura en tu registro, por ejemplo 3ro A."). The
grade is a `GChoiceChipRow` of six equal 48dp chips under a "GRADO" eyebrow;
the selected chip fills `primaryContainer` with a 2dp `primary` border, the
digit is `onPrimaryContainer` (`GChoiceChipRow.kt`: unselected digits are
`onSurface`). The name is a `GTextField`. The Area default is an
INFO `GBanner` (`surfaceContainerLow`, `control` radius) with the statement
on the first line and "No dicto todas las áreas" as its LINK action, so the
default path stays one tap and the exception is still visible. The multigrade
note is `bodySmall` helper text below the banner. *Terminar* is the
`bottomAction` PRIMARY `GButton`.

```
+------------------------------------------+
|  PASO 2 DE 2                             |
|  Tu primera sección                      |
|  Como figura en tu registro, por         |
|  ejemplo 3ro A.                          |
|                                          |
|  GRADO                                   |
|  +----+ +----+ +----+ +----+ +----+ +--+ |
|  | 1  | | 2  | |[3] | | 4  | | 5  | |6 | |
|  +----+ +----+ +----+ +----+ +----+ +--+ |
|                                          |
|  +------------------------------------+  |
|  | Nombre de la sección               |  |
|  | A                                  |  |
|  +------------------------------------+  |
|                                          |
|  +--------------------------------------+|
|  |  Todas las áreas quedan activas.     ||
|  |  No dicto todas las áreas          > ||
|  +--------------------------------------+|
|                                          |
|  Si enseñas en aula multigrado, crea     |
|  una sección por grado.                  |
+------------------------------------------+
|            [    Terminar     ]           |
+------------------------------------------+
```

```kotlin
data class SetupSectionUiState(
    val isLoading: Boolean = true,
    val grade: Grade? = null,
    val sectionName: String = "",
    val sectionNameError: SetupSectionMessage? = null,
    val canFinish: Boolean = false,
    val isSaving: Boolean = false,
)
```

Intents: `GradeSelected(grade: Grade)`, `SectionNameChanged(value: String)`,
`AreaSelectionClicked`, `FinishClicked`, `BackClicked`.

Effects: `NavigateToHome`, `NavigateToSectionAreas(sectionId: SectionId)`,
`NavigateBack`, `ShowMessage(message: SetupSectionMessage)`.

Copy changes (for the implementation ticket)

- `setup_section_helper_text`: "Ya casi. Después puedes crear todas las
  secciones que dictes." -> "Como figura en tu registro, por ejemplo 3ro A."
  (tells the Teacher the expected name format instead of generic
  encouragement).

---

## 3. Home

Mockup: [html](mockups/home.html) (no PNG until Phase 4 regenerates it from
the emulator).

Entry: launch with an existing School Year, or up from any Section.
Shows the active year, the current Period, the Backup reminder and the Sections.
Primary action: exactly one PRIMARY `GButton` on the whole screen, placed on
the first Section that still lacks today's attendance. FAB *Nueva sección*.

The screen leads with an eyebrow, "HOY · <weekday> <day> de <month>"
(for example "HOY · MARTES 10 DE SETIEMBRE"), above the Sections. A Section
already taken today collapses to a plain `GListItem` row — no secondary
button — with a chevron; tapping the row, anywhere on it, sends
`SectionClicked`, which the ViewModel routes to `NavigateToSectionDetail`.
A Section not yet taken keeps the expanded row with its own PRIMARY
button. Only one PRIMARY button exists on the whole screen, on
the first still-pending Section — every other pending Section (if a
multigrade Teacher has more than one) falls back to a SECONDARY button so
the primary action stays singular. The backup banner renders only when
`backupReminder` is non-null (the ViewModel only emits it when
`BackupStatus.isReminderDue` is true); otherwise no banner occupies that
space.

```
+------------------------------------------+
|  Gema  2026                    [dl]  [=] |
+------------------------------------------+
|  II Bimestre - faltan 24 dias             |
|                                          |
|  ! Ultimo respaldo hace 12 dias  Respaldar|
|                                          |
|  HOY · MARTES 10 DE SETIEMBRE             |
|  +--------------------------------------+|
|  | 3ro A                     30 alumnos ||
|  | Sin tomar - 12 niveles faltan         ||
|  |      [ Tomar asistencia de hoy ]     ||
|  +--------------------------------------+|
|  | 4to B                     27 alumnos >||
|  | 25 de 27 presentes                    ||
|  +--------------------------------------+|
|                                          |
|                                  (  +  ) |
+------------------------------------------+
```

```kotlin
data class HomeUiState(
    val isLoading: Boolean = true,
    val schoolYearId: SchoolYearId? = null,
    val schoolYearLabel: String = "",
    val todayLabel: String = "",
    val currentPeriodLabel: String? = null,
    val daysLeftInPeriod: Int? = null,
    val currentPeriodEndDate: LocalDate? = null,
    val sections: List<SectionRow> = emptyList(),
    val backupReminder: BackupReminder? = null,
)

data class SectionRow(
    val id: SectionId,
    val title: String,
    val studentCount: Int,
    val attendance: AttendanceDaySummary = AttendanceDaySummary(0, 0, 0),
    val missingLevelCount: Int? = null,
)

data class BackupReminder(
    val daysSinceLastBackup: Int,
    val hasEverBackedUp: Boolean,
)
```

`todayLabel` is (new): it carries the pre-formatted "HOY · <weekday> <day> de
<month>" eyebrow so the screen never formats a date itself. `SectionRow`
carries the domain `AttendanceDaySummary` rather than a separate
`isAttendanceTakenToday` flag: `attendance.isTaken` (`unmarkedCount <
totalCount`) already answers whether the Section renders as the expanded
row with a PRIMARY button or collapses to a `GListItem` row, so no
duplicate field is needed.

`missingLevelCount` is (new): sourced from
`GetMissingPeriodLevelCountUseCase(sectionId, currentPeriod.id)`, confirmed
present in `core:domain` and already wired into the Koin `AppModule`
(`app/src/main/kotlin/com/emm/gema/di/AppModule.kt`) — but only as a
dependency of `GetSectionDetailExtrasUseCase` for `SectionDetail`. Home does
not call this use case yet; `HomeViewModel`, `HomeUiState` and the Home Koin
wiring carry no reference to it today, so this field is new wiring for the
ticket, not existing behavior. Its value is: visible worked competencies
(hidden Areas excluded) times active, non-withdrawn Students, minus
recorded levels, for the current Period only; `null` when there is no
current Period. Render rule: the "· N niveles faltan" segment appears on
any Section row whenever `missingLevelCount` is greater than zero, appended
to the attendance status — an untaken row reads "Sin tomar · 12 niveles
faltan", a taken row reads "25 de 27 presentes · 12 niveles faltan". The
whole status line renders `GemaAccents.onWarningContainer` whenever
anything is pending on that row — untaken OR `missingLevelCount` greater
than zero — and `colorScheme.onSurfaceVariant` otherwise.

Intents: `SectionClicked(id: SectionId)`, `TakeAttendanceClicked(id: SectionId)`,
`AddSectionClicked`, `SchoolYearSwitcherClicked`, `OutOfPeriodClicked`,
`BackupReminderClicked`.

Effects: `NavigateToSectionDetail(sectionId: SectionId)`,
`NavigateToAttendanceDay(sectionId: SectionId, date: LocalDate)`,
`NavigateToSectionForm(schoolYearId: SchoolYearId, sectionId: SectionId?)`,
`NavigateToSchoolYears`, `NavigateToPeriods(schoolYearId: SchoolYearId)`,
`NavigateToBackup`.

Note: the top bar carries two actions, both reusing existing intents rather
than adding new destinations. `[dl]` (download) opens `Backup` through
`BackupReminderClicked` / `NavigateToBackup` — the same target as the
backup banner's own link. `[=]` (overflow) opens a one-item menu, "Cambiar
de año escolar", through `SchoolYearSwitcherClicked` / `NavigateToSchoolYears`.

Note: `currentPeriodLabel` is null when today falls outside every Period
(holidays, or a year whose dates were mistyped). The banner then reads
"Fuera de periodo" and links to `Periods` rather than hiding.

Note: a Section row's tap — its title or its chevron, in either render —
sends `SectionClicked`, which the ViewModel routes to
`NavigateToSectionDetail`. A still-pending Section additionally exposes an
inline PRIMARY button that takes today's attendance for that Section in one
tap, without a detour through the hub; an already-taken Section carries no
secondary button. Both read the same `SectionRow`; neither is destructive,
so neither confirms.

---

## 4. SchoolYears

Mockup: [html](mockups/school-years.html) (no PNG until Phase 4 regenerates
it from the emulator).

Entry: Home year switcher. Shows every School Year, marks the active one.
Primary action: `GExtendedFab` *Nuevo año*.

Registro layout: `GTopBar` with the title and the rule as its subtitle ("Toca
un año para activarlo. Nada se borra."), so the screen needs no explanatory
paragraph. Years are rows on the screen ground separated by hairlines, not
cards (new: rows replace `GYearCard`): `label` as `GTextStyle.NUMERAL` — the
only 20dp/600 `GText` style the code has, and the year label is itself a
numeral — then the `startDate` · `endDate` range and the `periodKind`,
formatted in the UI, and the section count as two `bodyLarge` lines in
`onSurfaceVariant`. The active
year carries a PRIMARY `GBadge` "ACTIVO" beside its label — a different badge
text than the Period picker's "ACTUAL", since an active year and a current
Period are different concepts. The whole row sends `YearClicked`;
"Periodos" is a trailing TEXT `GButton` with a chevron so it reads as a second
action rather than as the row. The `GExtendedFab` is the one dark object on
the screen.

```
+------------------------------------------+
|  <   Años escolares                      |
|      Toca un año para activarlo. Nada    |
|      se borra.                           |
+------------------------------------------+
|  2026  (ACTIVO)               Periodos > |
|  1 mar – 20 dic · 4 bimestres            |
|  2 secciones                             |
|  ----------------------------------------|
|  2025                         Periodos > |
|  1 mar – 19 dic · 3 trimestres           |
|  1 sección                               |
|  ----------------------------------------|
|                                          |
|                            [+ Nuevo año] |
+------------------------------------------+
```

```kotlin
data class SchoolYearsUiState(
    val isLoading: Boolean = true,
    val years: List<SchoolYearRow> = emptyList(),
)

data class SchoolYearRow(
    val id: SchoolYearId,
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodKind: PeriodKind,
    val sectionCount: Int,
    val isActive: Boolean,
)
```

Intents: `YearClicked(id: SchoolYearId)`, `PeriodsClicked(id: SchoolYearId)`,
`AddYearClicked`, `BackClicked`.

Effects: `NavigateToPeriods(id: SchoolYearId)`, `NavigateToSetupYear`,
`NavigateBack`.

Note: tapping a year switches the active year; it never deletes or archives.

Copy changes (for the implementation ticket)

- `school_years_subtitle`: "Toca un año para activarlo" -> "Toca un año para
  activarlo. Nada se borra." (states up front that switching years never
  deletes anything, principle 5).

---

## 5. Periods

Mockup: [html](mockups/periods.html) (no PNG until Phase 4 regenerates it
from the emulator).

Entry: SchoolYears, or the Home "Fuera de periodo" banner.
Shows the Periods of one School Year and which contains today.
Primary action: *Guardar*.

Registro layout: `GTopBar` with "Periodos 2026" and `periodKind`, formatted in
the UI, as the subtitle. Unlike SetupYear, this screen is the edit surface, so
each Period is a hairline row with its label (`titleMedium`), the "ACTUAL"
PRIMARY `GBadge` on the current one, and two inline 48dp `GDateField`s side by
side. When `overlapError` is non-null, the fields named by
`overlappingStartIds` and `overlappingEndIds` take the `error` border and
label, and an ERROR `GBanner` with a leading dot (new, #189) names the fix
under the list. *Guardar* is the `bottomAction` PRIMARY `GButton`, rendered
disabled (`surfaceContainerHigh` fill, `onSurfaceVariant` text) while
`canSave` is false; nothing else blocks.

```
+------------------------------------------+
|  <   Periodos 2026                       |
|      4 bimestres                         |
+------------------------------------------+
|  I Bimestre                              |
|  +---------------+  +----------------+   |
|  | Inicio    [c] |  | Fin        [c] |   |
|  | 01/03/2026    |  | 15/05/2026     |   |
|  +---------------+  +----------------+   |
|  ----------------------------------------|
|  II Bimestre  (ACTUAL)                   |
|  +---------------+  +----------------+   |
|  | Inicio    [c] |  | Fin (error)[c] |   |
|  | 18/05/2026    |  | 14/08/2026     |   |
|  +---------------+  +----------------+   |
|  ----------------------------------------|
|  III Bimestre                            |
|  +---------------+  +----------------+   |
|  | Inicio(error) |  | Fin        [c] |   |
|  | 10/08/2026    |  | 16/10/2026     |   |
|  +---------------+  +----------------+   |
|  ----------------------------------------|
|  IV Bimestre                             |
|  +---------------+  +----------------+   |
|  | 19/10/2026    |  | 20/12/2026     |   |
|  +---------------+  +----------------+   |
|                                          |
|  • Los periodos II y III se superponen.  |
|    Corrige las fechas marcadas para      |
|    guardar.                              |
+------------------------------------------+
|          [   Guardar (disabled)  ]       |
+------------------------------------------+
```

```kotlin
data class PeriodsUiState(
    val isLoading: Boolean = true,
    val schoolYearLabel: String = "",
    val periodKind: PeriodKind = PeriodKind.BIMESTER,
    val periods: List<PeriodRow> = emptyList(),
    val overlapError: PeriodRangeError? = null,
    val overlappingStartIds: Set<PeriodId> = emptySet(),
    val overlappingEndIds: Set<PeriodId> = emptySet(),
    val canSave: Boolean = false,
)

data class PeriodRow(
    val id: PeriodId,
    val number: Int,
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isCurrent: Boolean,
)
```

Intents: `StartDateChanged(id: PeriodId, value: LocalDate)`,
`EndDateChanged(id: PeriodId, value: LocalDate)`, `SaveClicked`, `BackClicked`.

Effects: `NavigateBack`, `ShowMessage(message: PeriodsMessage)`.

Copy changes (for the implementation ticket)

No copy changes: the title, the "ACTUAL" current-Period badge and the visible
labels already match `setup_periods_title` and `setup_periods_current_badge`.

---

## 6. SectionForm

Mockup: [html](mockups/section-form.html) · delete confirmation
[html](mockups/section-form-delete.html) (no PNG until Phase 4 regenerates
them from the emulator).

Entry: Home FAB (create), SectionDetail overflow (rename).
Shows Grade and name. Primary action: *Guardar*.

Registro layout: `GTopBar` reads "Nueva sección" when `sectionId` is null and
"Editar sección" with `sections_form_subtitle` — grade, section name and
student count, e.g. "3ro A · 30 alumnos" — as subtitle otherwise. The grade
`GChoiceChipRow` and the name `GTextField` are the same controls as
SetupSection. When `canDelete` is true, a "ZONA DE RIESGO" eyebrow groups a
DESTRUCTIVE `GButton` "Eliminar sección" with one `bodySmall` helper line
("Borra alumnos, asistencia y niveles de esta sección. Te preguntamos
antes."), placed after a 32dp gap so it never sits next to *Guardar*. Create
mode is the same screen without that group. *Guardar* is the `bottomAction`
PRIMARY `GButton`.

```
+------------------------------------------+
|  <   Editar sección                      |
|      3ro A · 30 alumnos                  |
+------------------------------------------+
|  GRADO                                   |
|  +----+ +----+ +----+ +----+ +----+ +--+ |
|  | 1  | | 2  | |[3] | | 4  | | 5  | |6 | |
|  +----+ +----+ +----+ +----+ +----+ +--+ |
|                                          |
|  +------------------------------------+  |
|  | Nombre                             |  |
|  | A                                  |  |
|  +------------------------------------+  |
|                                          |
|  ZONA DE RIESGO                          |
|  [        Eliminar sección           ]   |
|  Borra alumnos, asistencia y niveles de  |
|  esta sección. Te preguntamos antes.     |
+------------------------------------------+
|             [   Guardar   ]              |
+------------------------------------------+
```

The confirmation is a `GDialog(isDestructive = true)`. Its content is a
short statement followed by the three `DeleteConfirmation` counts as
hairline rows, each count in `NUMERAL` style beside its noun, so the Teacher
reads what is lost before reaching the confirm. `periodLevelCount` is
`GetSectionDeletionImpactUseCase` summing `GetPeriodLevelCountUseCase` across
every Period of the Section, not one Period, so its row reads "niveles de
logro" rather than "niveles del periodo" — matching `sections_form_delete_message`.
`isDestructive` makes `GDialog`'s confirm a DESTRUCTIVE `GButton`
("Eliminar"): `GButton.kt` renders that variant as an outlined button
(`error`-colored 1dp border and text, no fill), not filled and not a plain
text button; the dismiss ("Cancelar") stays a TEXT `GButton`.

Copy changes (for the implementation ticket)

- `sections_form_delete_dialog_title`: "¿Eliminar la sección?" ->
  "¿Eliminar 3ro A?" (names the Section, so the Teacher confirms the right
  one).
- `sections_form_delete_message`: "Se perderán %1$d estudiantes, %2$d días
  de asistencia y %3$d niveles de logro." -> "Se borra de forma definitiva:"
  followed by the three counts as rows ("alumnos", "días de asistencia",
  "niveles de logro"); "alumnos" matches `sections_form_student_count` and
  the rest of the app.
- New helper under the delete button: "Borra alumnos, asistencia y niveles
  de esta sección. Te preguntamos antes."

```
+------------------------------------------+
|  ¿Eliminar 3ro A?                        |
|  Se borra de forma definitiva:           |
|  ----------------------------------------|
|    30  alumnos                           |
|  ----------------------------------------|
|    84  días de asistencia                |
|  ----------------------------------------|
|   112  niveles de logro                  |
|  ----------------------------------------|
|                    Cancelar    Eliminar  |
+------------------------------------------+
```

```kotlin
data class SectionFormUiState(
    val isLoading: Boolean = true,
    val sectionId: SectionId? = null,
    val grade: Grade? = null,
    val sectionName: String = "",
    val sectionNameTouched: Boolean = false,
    val sectionNameError: SectionFormMessage? = null,
    val canSave: Boolean = false,
    val canDelete: Boolean = false,
    val studentCount: Int = 0,
    val deleteConfirmation: DeleteConfirmation? = null,
)

data class DeleteConfirmation(
    val studentCount: Int,
    val attendanceDayCount: Int,
    val periodLevelCount: Int,
)
```

Intents: `GradeSelected(grade: Grade)`, `SectionNameChanged(value: String)`,
`SaveClicked`, `DeleteClicked`, `DeleteConfirmed`, `DeleteDismissed`,
`BackClicked`.

Effects: `NavigateBack`, `ShowMessage(message: SectionFormMessage)`.

Note: `deleteConfirmation` non-null renders the dialog naming what is lost
(US 14). An empty Section still confirms, but with zero counts.

---

## 7. SectionAreas

Mockup: [html](mockups/section-areas.html) (no PNG until Phase 4 regenerates
it from the emulator).

Entry: SectionDetail overflow, or the SetupSection link.
Shows every primary Area with a switch. Saves on toggle.

Registro layout: `GTopBar` "Áreas · 3ro A" with "Cada cambio se guarda solo"
as its subtitle, the same reassurance AttendanceDay carries. One `bodyLarge`
line in `onSurfaceVariant` states the rule, then nine `GSwitchRow`s on
hairlines, `GemaSpacing.compactRowHeight` (52dp) minimum, name in `bodyLarge`.
The recorded-level note is no longer a banner under the list: when an Area
with `recordedLevelCount > 0` is off, that count renders as the row's
`subtitle` (new: `GSwitchRow` needs a subtitle color parameter — its subtitle
color is fixed to `onSurfaceVariant` today) in `GemaAccents.onWarningContainer`
text ("12 niveles registrados. Quedan guardados y dejan de exportarse."),
next to the switch that caused it. The note keeps the consequence `sections_areas_recorded_levels`
states: the levels stay saved and stop being exported. The text-only use of
`onWarningContainer` follows the note under the color table in `system.md`.
Rows with a long name wrap to two lines rather than truncating.

```
+------------------------------------------+
|  <   Áreas · 3ro A                       |
|      Cada cambio se guarda solo          |
+------------------------------------------+
|  Apaga las áreas que no dictas. Nada se  |
|  borra: puedes volver a encenderlas.     |
|  ----------------------------------------|
|  Comunicación                     (ON )  |
|  ----------------------------------------|
|  Matemática                       (ON )  |
|  ----------------------------------------|
|  Personal Social                  (ON )  |
|  ----------------------------------------|
|  Ciencia y Tecnología             (ON )  |
|  ----------------------------------------|
|  Arte y Cultura                   (ON )  |
|  ----------------------------------------|
|  Educación Física                 ( OFF) |
|  12 niveles registrados. Quedan          |
|  guardados y dejan de exportarse.        |
|  ----------------------------------------|
|  Educación Religiosa              (ON )  |
|  ----------------------------------------|
|  Inglés                           ( OFF) |
|  ----------------------------------------|
|  Castellano como segunda lengua   ( OFF) |
|  ----------------------------------------|
+------------------------------------------+
```

```kotlin
data class SectionAreasUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val areas: List<AreaToggleRow> = emptyList(),
)

data class AreaToggleRow(
    val id: Area,
    val name: String,
    val isActive: Boolean,
    val recordedLevelCount: Int,
)
```

Intents: `AreaToggled(id: Area, isActive: Boolean)`, `BackClicked`.

Effects: `NavigateBack`, `ShowMessage(message: SectionAreasMessage)`.

Copy changes (for the implementation ticket)

- `sections_areas_title`: "Áreas de %1$s" -> "Áreas · %1$s" (the middle dot
  is the top-bar separator every Registro screen uses).
- `sections_areas_subtitle`: "Cada toque se guarda solo" -> "Cada cambio se
  guarda solo" (a switch is a change, not a toque; AttendanceDay keeps
  "toque").
- `sections_areas_recorded_levels`: same words, moved from the footer banner
  to the subtitle of the row that is off; the plural keeps "Quedan guardados
  y dejan de exportarse."

---

## 8. SectionDetail

Mockup: [html](mockups/section-detail.html) (no PNG until Phase 4
regenerates it from the emulator).

Entry: Home. The hub for one Section.
Primary action: *Tomar asistencia de hoy*.

Registro layout: `GTopBar` with `sectionTitle`, "`studentCount` alumnos ·
`currentPeriodLabel`" as the subtitle, and the overflow (Áreas / Renombrar)
as its one action. `hasStoredTemplate` renders as a `bodySmall` line with a
`primary` check glyph, "Plantilla SIAGIE cargada", and renders nothing when
false. The daily block has no card, no border, no rounded container — just
the "HOY · <weekday> <day> de <month>" eyebrow, `todayAttendanceSummary` as
`bodyLarge` in `GemaAccents.onWarningContainer` while it reads *Sin tomar*
and `onSurfaceVariant` once taken, then the single PRIMARY `GButton`. The
five destinations are `GListItem` rows on full-width hairlines (edge to
edge, outside the screen gutter so row text lines up with the eyebrow above
it — a leading hairline above the first row, a closing one below the last,
no leading icons) with
`trailingText` for the counts: `studentCount`, `missingPeriodLevelCount` as
"N faltan" via `GListItem`'s `trailingTextColor` (#216) in
`onWarningContainer` text when greater than zero, `activityCount`. Both
pending lines use `onWarningContainer` as text
on `surface`, the exception the note under the color table in `system.md`
allows. The last row reads *Entregar*, the goal named in `flows.md` §8, and
still sends `ExportClicked`.

```
+------------------------------------------+
|  <   3ro A                          [.:.]|
|      30 alumnos · II Bimestre            |
+------------------------------------------+
|  ✓ Plantilla SIAGIE cargada              |
|                                          |
|  HOY · MARTES 10 DE SETIEMBRE            |
|  Sin tomar                               |
|  [      Tomar asistencia de hoy      ]   |
|                                          |
|  ----------------------------------------|
|  Alumnos                          30   > |
|  ----------------------------------------|
|  Asistencia                            > |
|  ----------------------------------------|
|  Niveles del periodo        12 faltan  > |
|  ----------------------------------------|
|  Actividades                       5   > |
|  ----------------------------------------|
|  Entregar                              > |
|  ----------------------------------------|
+------------------------------------------+
```

```kotlin
data class SectionDetailUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val studentCount: Int = 0,
    val currentPeriodLabel: String? = null,
    val hasStoredTemplate: Boolean = false,
    val todayLabel: String = "",
    val todayAttendanceSummary: String = "",
    val isTodayAttendanceTaken: Boolean = false,
    val missingPeriodLevelCount: Int = 0,
    val activityCount: Int = 0,
)
```

`todayLabel` is the shared "HOY · <weekday> <day> de <month>" eyebrow —
`core:ui`'s `todayLabelOf` (next to `DateLabels.kt`), the same formatter and
`DateNameProvider` Home uses. `todayAttendanceSummary` reads *Sin tomar* until
the first Student of the day is recorded, then *N de M presentes*;
`isTodayAttendanceTaken` (from `AttendanceDaySummary.isTaken`) is what the
status line's color reads, never a string comparison. Both this screen and
Home's section cards format the same `AttendanceDaySummary` from
`core:domain`; neither counts
Students itself. `currentPeriodLabel`, `hasStoredTemplate`,
`missingPeriodLevelCount` and `activityCount` arrive with their own tickets.

Intents: `TakeAttendanceClicked`, `StudentsClicked`, `AttendanceClicked`,
`PeriodLevelsClicked`, `ActivitiesClicked`, `ExportClicked`, `AreasClicked`,
`RenameClicked`, `BackClicked`.

Effects: `NavigateToAttendanceDay(sectionId: SectionId, date: LocalDate)`,
`NavigateToStudents(sectionId: SectionId)`,
`NavigateToPeriodLevels(sectionId: SectionId)`,
`NavigateToActivities(sectionId: SectionId)`,
`NavigateToExport(sectionId: SectionId)`,
`NavigateToSectionAreas(sectionId: SectionId)`,
`NavigateToSectionForm(schoolYearId: SchoolYearId, sectionId: SectionId)`,
`NavigateBack`.

Copy changes (for the implementation ticket)

- `sections_detail_export`: "Exportar" -> "Entregar" (the Teacher's goal,
  `flows.md` §8; the Export screen title already reads "Entregar").
- `sections_detail_attendance_today_label` ("ASISTENCIA DE HOY") is removed:
  the eyebrow now reads `todayLabel`, sourced from `core:ui`'s shared
  `today_label` string via `todayLabelOf`, the same one Home renders.
- New status line above the primary button: `todayAttendanceSummary`
  ("Sin tomar" / "N de M presentes"), no new string.

---

## 9. Students

Mockup: [html](mockups/students.html) (no PNG until Phase 4 regenerates it
from the emulator).

Entry: SectionDetail. Lists Students by surname.
Primary action: `GExtendedFab` *Agregar alumno*.

Registro layout: `GTopBar` "Alumnos · 3ro A" with the active count and the
sort rule as subtitle ("30 activos · por apellido"), and two glyph-only
`GIconButton` actions: a search trigger (dispatches `SearchToggled`) next to
*Importar* (new: today *Importar* is the only action, a SECONDARY `GButton`
with a label and an icon; the glyphs keep the top bar quiet and the FAB as
the only labelled action). The `GSearchField` (48dp, `surface` fill, 1dp
`outline` border, `control` radius, placeholder "Buscar por apellido": the
target `components.md` schedules for the Students ticket; today it is a
filled `surfaceVariant` pill) renders only while `isSearchVisible` is
true (today's screen always shows it and its `UiState` lacks the flag; the
flag is the contract above). Each Student is a `GListItem` on a hairline:
`displayName` as `bodyLarge` (the component's default title style; wraps,
never truncates), `studentCode` as a tabular `bodySmall` subtitle in
`onSurfaceVariant` (new: `GListItem` renders subtitles as `bodyMedium`
without tabular figures today), a chevron. The withdrawn
roster collapses under a full-bleed `GGroupHeader` ("RETIRADOS (2)",
`labelSmall`, 48dp) with an expand chevron; its fill is the
`surfaceContainerLow` swap `components.md` already schedules for the
Students ticket (today's default is `surfaceVariant`). The
`GExtendedFab` is the one dark object on the screen.

```
+------------------------------------------+
|  <   Alumnos · 3ro A          [search][up]|
|      30 activos · por apellido           |
+------------------------------------------+
|  +------------------------------------+  |
|  | (o) Buscar por apellido            |  |
|  +------------------------------------+  |
|  ----------------------------------------|
|  APAZA CONDORI, YESENIA                > |
|  12345678901234                          |
|  ----------------------------------------|
|  CCAHUANA FLORES, MARÍA                > |
|  12345678901235                          |
|  ----------------------------------------|
|  HUANCA RÍOS, DIEGO                    > |
|  12345678901236                          |
|  ----------------------------------------|
|  RETIRADOS (2)                         v |
|                                          |
|                       [+ Agregar alumno] |
+------------------------------------------+
```

The top bar carries the search trigger and *Importar*; *Agregar alumno* is
the extended FAB, bottom-right.

```kotlin
data class StudentsUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val query: String = "",
    val isSearchVisible: Boolean = false,
    val activeStudents: List<StudentRow> = emptyList(),
    val withdrawnStudents: List<StudentRow> = emptyList(),
    val isWithdrawnExpanded: Boolean = false,
)

data class StudentRow(
    val id: StudentId,
    val displayName: String,
    val studentCode: String,
    val withdrawalDate: LocalDate?,
)
```

Intents: `QueryChanged(value: String)`, `StudentClicked(id: StudentId)`,
`AddStudentClicked`, `ImportClicked`, `ImportFilePicked(uri: String)`,
`SearchToggled`, `WithdrawnSectionToggled`, `ReactivateClicked(id: StudentId)`,
`BackClicked`.

Effects: `NavigateToStudentForm(sectionId: SectionId, studentId: StudentId?)`,
`OpenDocumentPicker(mimeTypes: List<String>)`,
`NavigateToImportPreview(sectionId: SectionId, uri: String)`, `NavigateBack`,
`ShowMessage(message: StudentsMessage)`.

Copy changes (for the implementation ticket)

- `GSearchField` placeholder (`StudentsScreen.kt`, hardcoded, no string
  resource key yet): "Buscar" -> "Buscar por apellido" (states the list is
  searched by surname, matching the sort rule in the subtitle).

---

## 10. StudentForm

Mockup: [html](mockups/student-form.html) (no PNG until Phase 4 regenerates
it from the emulator).

Entry: Students. Add or edit one Student. Primary action: *Guardar*.

Registro layout: `GTopBar` "Editar alumno" (or "Nuevo alumno") with the
Section as subtitle. Two `GTextField`s, each with its supporting line: the
code (numeric keyboard, tabular value, "14 de 14 dígitos") and the name
("Apellidos primero, como en SIAGIE."). When `hasSiagieId` is true an INFO
`GBanner` with a file glyph sits between the fields and the state group
("Viene de la plantilla SIAGIE. Si cambias el código, la exportación no lo
encontrará.") (new: replaces `GCompactNote`, which is fixed to
`surfaceVariant` and an Info icon; one supporting component fewer). "ESTADO"
is an eyebrow over a two-segment `GSegmentedPicker` (Activo / Retirado); the
screen passes `activeContainerColor = surfaceContainerHigh` and
`activeContentColor = onSurface` so the selected *Retirado* segment reads as
a state, not a success (today the call uses the `primaryContainer` default). The withdrawal `GDateField` and its `bodySmall` helper
("Desde esa fecha deja de aparecer en la asistencia. Nada se borra.") render
only while `isWithdrawn` is true. *Guardar* is the `bottomAction` PRIMARY
`GButton`.

```
+------------------------------------------+
|  <   Editar alumno                       |
|      3ro A                               |
+------------------------------------------+
|  +------------------------------------+  |
|  | Código del estudiante              |  |
|  | 12345678901234                     |  |
|  +------------------------------------+  |
|    14 de 14 dígitos                      |
|  +------------------------------------+  |
|  | Apellidos y nombres                |  |
|  | HUANCA RÍOS, DIEGO                 |  |
|  +------------------------------------+  |
|    Apellidos primero, como en SIAGIE.    |
|                                          |
|  +--------------------------------------+|
|  | [f] Viene de la plantilla SIAGIE. Si ||
|  |     cambias el código, la exportación||
|  |     no lo encontrará.                ||
|  +--------------------------------------+|
|                                          |
|  ESTADO                                  |
|  +------------------+-----------------+  |
|  |      Activo      |    [Retirado]   |  |
|  +------------------+-----------------+  |
|  +------------------------------------+  |
|  | Fecha de retiro                [c] |  |
|  | 04/09/2026                         |  |
|  +------------------------------------+  |
|  Desde esa fecha deja de aparecer en la  |
|  asistencia. Nada se borra.              |
+------------------------------------------+
|             [   Guardar   ]              |
+------------------------------------------+
```

```kotlin
data class StudentFormUiState(
    val isLoading: Boolean = true,
    val studentId: StudentId? = null,
    val studentCode: String = "",
    val studentCodeError: StudentCodeError? = null,
    val studentCodeHint: String = "",
    val fullName: String = "",
    val fullNameError: FullNameError? = null,
    val isWithdrawn: Boolean = false,
    val withdrawalDate: LocalDate? = null,
    val withdrawalDateError: WithdrawalDateError? = null,
    val canSave: Boolean = false,
    val hasSiagieId: Boolean = false,
    val sectionTitle: String = "",
    val isStudentCodeValid: Boolean = false,
)
```

Intents: `StudentCodeChanged(value: String)`, `FullNameChanged(value: String)`,
`WithdrawnToggled(isWithdrawn: Boolean)`,
`WithdrawalDateChanged(value: LocalDate)`, `SaveClicked`, `BackClicked`.

Effects: `NavigateBack`, `ShowMessage(message: StudentFormMessage)`.

Note: `hasSiagieId` drives a read-only hint that this Student came from a SIAGIE
Template, so editing the code by hand may break the round-trip.

---

## 11. ImportPreview

Mockup: [html](mockups/import-preview.html) · rejected
[html](mockups/import-preview-rejected.html) (no PNG until Phase 4
regenerates them from the emulator).

Entry: the Students top bar, after the system document picker returns a URI.
Shows what the import will do. Nothing is written until *Aplicar*.

Registro layout: `GTopBar` "Importar de SIAGIE" whose subtitle states the
rule ("Nada se escribe hasta que apliques"). `fileName` renders as
`titleMedium` with a file glyph, then "`sectionTitle` · `rosterSize` alumnos
en el archivo" in `onSurfaceVariant`. The three groups are `GListItem` rows
on hairlines with the count as `NUMERAL` trailing text and an expand chevron
(down closed, up open); the group matching `expandedGroup` lists its rows
below, `GCheckRow`s for the withdrawals (default on) with one `bodySmall`
helper ("Desmarca a quien siga en el aula."). The reassurance line stays as
`bodySmall`. The `bottomAction` is a `Row` of two `GButton`s: SECONDARY
*Cancelar* sized to its own text, PRIMARY *Aplicar importación* filling the
remaining width; `isApplying` sets `isBusy` on the primary. `created` and
`updated` do not have to sum to `rosterSize`: `SiagieImportPlanner` de-dups
the roster by Student Code before counting, and `updated` drops any Student
the file did not actually change, so both groups can undercount the file.

```
+------------------------------------------+
|  <   Importar de SIAGIE                  |
|      Nada se escribe hasta que apliques  |
+------------------------------------------+
|  [f] 3 Primaria EBR.xlsx                 |
|  3ro A · 30 alumnos en el archivo        |
|                                          |
|  ----------------------------------------|
|  Se crearán                         4  v |
|  ----------------------------------------|
|  Se actualizarán                   26  v |
|  ----------------------------------------|
|  Se propondrán como retirados       2  ^ |
|  [x] LÓPEZ SILVA, ANA                    |
|  [x] TORRES PINO, LUIS                   |
|      Desmarca a quien siga en el aula.   |
|  ----------------------------------------|
|                                          |
|  Nada se borra. Los retirados conservan  |
|  su asistencia y sus niveles.            |
+------------------------------------------+
|  [ Cancelar ]  [  Aplicar importación  ] |
+------------------------------------------+
```

A roster whose Student Code is missing or malformed in the middle is rejected
with the row number, never truncated at that row.

Rejection state replaces the body. A `GFileCard` with `fileName` (no
`subtitle`, which is optional in `core:ui`) sits above an ERROR `GBanner`
that carries `reason` (a leading dot in its `icon` slot); when the rejection
has an `expected`/`found` pair it renders as two
`GListItem` rows with the value as `trailingText`, the found value in `error`
ink through `trailingTextColor`, then one `bodyLarge` line renders
`instruction` and says nothing changed. The `bottomAction` is always a single
SECONDARY `GButton` *Volver a alumnos* that sends `CancelClicked`; the screen
has no re-pick intent.

`SiagieImportRejection` has five cases; only two of them carry an
`expected`/`found` pair, and only those two have anything to do with the
Section being wrong, so "abre la sección correcta" is not part of the other
three. `ImportRejection` carries the per-case `instruction` and the
found-row's `foundLabel` as `UiState` fields, so the screen never hardcodes
either:

- `NotASiagieTemplate`: `reason` "Este archivo no es una plantilla de
  SIAGIE", no pair. `instruction`: "Elige otro archivo. No se cambió nada."
  — the file itself is unusable, no Section is involved.
- `EmptyRoster`: `reason` "La plantilla no tiene alumnos", no pair.
  `instruction`: "Elige otro archivo. No se cambió nada."
- `MalformedRow(row)`: `reason` names the row, e.g. "La fila 15 del archivo no
  tiene un código de estudiante válido", no pair. `instruction`: "Corrige el
  archivo y vuelve a intentarlo. No se cambió nada." — the file needs fixing,
  not a different Section.
- `GradeMismatch(expected, found)`: `reason` "El grado de la plantilla no
  coincide con la sección", pair "Sección abierta" / `foundLabel` "Grado en
  el archivo". `instruction`: "Elige otro archivo o abre la sección
  correcta. No se cambió nada."
- `SectionMismatch(expected, found)`: `reason` "Este archivo no es de esta
  sección", pair "Sección abierta" / `foundLabel` "Archivo" (the wireframe
  below shows this case). `instruction`: "Elige otro archivo o abre la
  sección correcta. No se cambió nada."

```
+------------------------------------------+
|  <   Importar de SIAGIE                  |
|      Nada se escribe hasta que apliques  |
+------------------------------------------+
|  [f] 4 Primaria EBR.xlsx                 |
|                                          |
|  • Este archivo no es de esta sección    |
|                                          |
|  ----------------------------------------|
|  Sección abierta     3ro A               |
|  ----------------------------------------|
|  Archivo             4to B               |
|  ----------------------------------------|
|  Elige otro archivo o abre la sección    |
|  correcta. No se cambió nada.            |
+------------------------------------------+
|         [   Volver a alumnos   ]         |
+------------------------------------------+
```

```kotlin
data class ImportPreviewUiState(
    val isLoading: Boolean = true,
    val fileName: String = "",
    val sectionTitle: String = "",
    val rosterSize: Int = 0,
    val rejection: ImportRejection? = null,
    val created: List<ImportStudentRow> = emptyList(),
    val updated: List<ImportStudentRow> = emptyList(),
    val proposedWithdrawals: List<ImportWithdrawalRow> = emptyList(),
    val expandedGroup: ImportGroup? = null,
    val isApplying: Boolean = false,
) {
    val canApply: Boolean
        get() = !isLoading && !isApplying && rejection == null
}

data class ImportStudentRow(
    val studentCode: String,
    val displayName: String,
)

data class ImportWithdrawalRow(
    val studentId: StudentId,
    val displayName: String,
    val isSelected: Boolean,
)

data class ImportRejection(
    val reason: ImportRejectionReason,
    val instruction: ImportInstruction,
    val expected: String?,
    val found: String?,
    val foundLabel: ImportFoundLabel?,
)

sealed interface ImportRejectionReason {
    data object NotASiagieTemplate : ImportRejectionReason
    data object EmptyRoster : ImportRejectionReason
    data class MalformedRow(val row: Int) : ImportRejectionReason
    data object GradeMismatch : ImportRejectionReason
    data object SectionMismatch : ImportRejectionReason
}

enum class ImportInstruction { PICK_ANOTHER_FILE, FIX_FILE, PICK_ANOTHER_FILE_OR_OPEN_SECTION }

enum class ImportFoundLabel { GRADE, SECTION }

enum class ImportGroup { CREATED, UPDATED, WITHDRAWN }
```

The ViewModel emits these typed values in `ImportRejection`; the screen never
holds copy directly and resolves `reason`, `instruction` and `foundLabel` to
text with `stringResource` per case. `canApply` is computed on
`ImportPreviewUiState` as `!isLoading && !isApplying && rejection == null`.

Intents: `GroupToggled(group: ImportGroup)`,
`WithdrawalToggled(studentId: StudentId, isSelected: Boolean)`, `ApplyClicked`,
`CancelClicked`, `BackClicked`.

Effects: `NavigateBack`, `ShowMessage(message: ImportPreviewMessage)`.

---

## 12. AttendanceDay

Mockup: [html](mockups/attendance-day.html) (no PNG until Phase 4
regenerates it from the emulator).

Entry: SectionDetail primary action, or the Attendance row.
The daily workhorse. Every tap persists one row; there is no save button.

```
+------------------------------------------+
|  <   Asistencia - 3ro A            [cal] |
|       Cada toque se guarda solo          |
+------------------------------------------+
|   <    Mar 10 set 2026    >              |
|  +------------------------------------+  |
|  |28 de 30 presentes [Todos presentes]|  |
|  |2 sin marcar                        |  |
|  +------------------------------------+  |
|  ------------------------------------    |
|  ACOSTA RIVERA, Luz Maria                |
|  +------+------+------+------+           |
|  |  P   |  T   |  F   |  FJ  |           |
|  +======+------+------+------+           |
|  ------------------------------------    |
| :BAUTISTA QUISPE, Jose : sin marcar      |
| :+------+------+------+------+:          |
| :|  P   |  T   |  F   |  FJ  |:          |
| :+------+------+------+------+:          |
|  ------------------------------------    |
|  CCAHUANA MAMANI, Rosa                   |
|  +------+------+------+------+           |
|  |  P   |  T   |  F   |  FJ  |           |
|  +------+------+------+======+           |
+------------------------------------------+
|  Resumen del mes                      >  |
+------------------------------------------+
```

`P` presente, `T` tardanza, `F` falta, `FJ` falta justificada. The student
name sits on its own line, with the four-segment toggle below it — never on
the same line — so a long name never squeezes the toggle at font scale 1.3.
The selected segment carries a filled background plus weight 600, so the
state is not colour-only. The summary strip above the list reads as a
`numeral` count ("28 de 30 presentes") with "N sin marcar" beneath it in
`GemaAccents.onWarningContainer` text; "Todos presentes" sits beside it as a
SECONDARY button. The strip is a `surfaceContainerLow` block with `control`
radius, not a card. Each student row sits under an `outlineVariant` hairline
(`GDivider`) and the name takes the remaining width, so the trailing
"sin marcar" label never wraps. "Resumen del mes" is a footer pinned below
the scrolling list, not the list's last row.
The `:`-bordered row is an unmarked Student: `warningContainer` row tint plus
a dashed outline and a trailing "sin marcar" label, not colour alone, so it
stands out on a low-end screen in daylight.

```kotlin
data class AttendanceDayUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val date: LocalDate? = null,
    val canGoForward: Boolean = false,
    val presentCount: Int = 0,
    val totalCount: Int = 0,
    val unmarkedCount: Int = 0,
    val rows: List<AttendanceRow> = emptyList(),
) {
    val canMarkAllPresent: Boolean get() = unmarkedCount > 0
}

data class AttendanceRow(
    val studentId: StudentId,
    val displayName: String,
    val status: AttendanceStatus,
    val isRecorded: Boolean,
)
```

Intents: `StatusSelected(studentId: StudentId, status: AttendanceStatus)`,
`MarkAllPresent`, `PreviousDayClicked`, `NextDayClicked`,
`DatePicked(value: LocalDate)`, `MonthlySummaryClicked`, `BackClicked`.

Effects: `NavigateToAttendanceMonth(sectionId: SectionId, month: YearMonth)`,
`NavigateBack`, `ShowMessage(message: AttendanceDayMessage)`.

Notes:

- `isRecorded = false` means the row is showing the present default and nothing
  is stored yet (US 25, 26). The first tap creates the row. `unmarkedCount`
  counts these rows and drives the summary strip's "N sin marcar" text line.
- `MarkAllPresent` records present for every `isRecorded = false` row in one
  action; a row already marked (present, late, absent or justified) is left as
  the Teacher set it, and the action disables itself once nothing is unmarked.
- `MonthlySummaryClicked` and its effect arrive with AttendanceMonth (#13).
- `canGoForward` is false on today; future dates are unreachable rather than
  rejected after the fact.
- Withdrawn Students are absent from `rows` for dates on or after their
  withdrawal date (US 29).

---

## 13. AttendanceMonth

Mockup: [html](mockups/attendance-month.html) (no PNG until Phase 4
regenerates it from the emulator).

Entry: AttendanceDay footer. Monthly counts per Student, plus the SIAGIE
attendance export. Primary action: *Exportar el mes a SIAGIE*.

Registro layout: `GTopBar` "Asistencia · 3ro A", subtitle
"Resumen del mes", and a `GIconButton` calendar action that opens
`GMonthPickerDialog` (`MonthPicked`), as today (`GCalendarIconButton` wraps a
day picker and does not fit a `YearMonth`). Under it the month stepper mirrors the
day stepper on AttendanceDay: `GIconButton` pair around the month label
(`titleMedium`), forward disabled (`outline` glyph) at the current month. The
table is full-bleed (`contentGutter = false`): a `GTableHeaderBand` on
`surfaceContainerLow` with "ALUMNO" and the four status letters as
`labelSmall`, then one `GTableRow` per Student, 52dp minimum, the name as
`bodyLarge` wrapping to two lines (new: today it is one line with an ellipsis
and no added weight; wrapping is what survives font scale 1.3), and four 40dp count
columns in tabular figures. A zero count renders in `outline` color (today
`outlineVariant`, too faint on white) so the non-zero counts carry the row;
color is never the only signal, the digit is there. `recordedDayCount` is
the `bodySmall` footer line. The PRIMARY `bottomAction` is disabled while
`canExport` is false, and `isExporting` sets `isBusy` (supported by
`GButton`, not passed today). `exportUnavailableReason` stays unrendered in
this batch: the ViewModel never assigns it, so a banner for it would never
show.

```
+------------------------------------------+
|  <   Asistencia · 3ro A            [cal] |
|      Resumen del mes                     |
+------------------------------------------+
|   <        setiembre 2026        (>)     |
|  ----------------------------------------|
|  ALUMNO                 P    T    F   FJ |
|  ----------------------------------------|
|  APAZA CONDORI, YESENIA 18   1    0    1 |
|  ----------------------------------------|
|  CCAHUANA FLORES, MARÍA 15   2    3    0 |
|  ----------------------------------------|
|  HUANCA RÍOS, DIEGO     20   0    0    0 |
|  ----------------------------------------|
|  MAMANI TORRES, LUIS    12   1    6    1 |
|  Alberto                                 |
|  ----------------------------------------|
|  20 días de clase registrados            |
+------------------------------------------+
|      [   Exportar el mes a SIAGIE   ]    |
+------------------------------------------+
```

```kotlin
data class AttendanceMonthUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val month: YearMonth? = null,
    val recordedDayCount: Int = 0,
    val rows: List<AttendanceMonthRow> = emptyList(),
    val canExport: Boolean = false,
    val exportUnavailableReason: String? = null,
    val isExporting: Boolean = false,
)

data class AttendanceMonthRow(
    val studentId: StudentId,
    val displayName: String,
    val countsByStatus: Map<AttendanceStatus, Int>,
)
```

Intents: `PreviousMonthClicked`, `NextMonthClicked`,
`MonthPicked(value: YearMonth)`, `ExportClicked`,
`TemplatePicked(uri: String)`, `BackClicked`.

Effects: `OpenDocumentPicker(mimeTypes: List<String>)`,
`ShareFile(path: String, mimeType: String)`, `NavigateBack`,
`ShowMessage(message: AttendanceMonthMessage)`.

Note (ADR 0019): `canExport` reflects `recordedDayCount > 0`, not a stored
template — the attendance template is a new file every month, so there is
nothing to store. `ExportClicked` opens the system document picker
(`OpenDocumentPicker`); the picked file drives `TemplatePicked`, which fills
that exact file and hands the result to `ShareFile`. This differs from the
grades card in `Export` (#20), which reads a template stored at import time.

Copy changes (for the implementation ticket)

- Primary/bottom-action button (`AttendanceMonthScreen.kt`, hardcoded, no
  string resource key yet): "Exportar el mes" -> "Exportar el mes a SIAGIE"
  (names the export target, matching how ImportPreview and Export name
  SIAGIE explicitly).

---

## 14. WorkedCompetencies

Mockup: [html](mockups/worked-competencies.html) (no PNG until Phase 4
regenerates it from the emulator).

Entry: PeriodLevels empty state or its top bar. One Area, one Period.
Saves on toggle.

Registro layout: `GTopBar` "Competencias trabajadas" with "`areaName` ·
`periodLabel`" as subtitle. One `bodyLarge` line in `onSurfaceVariant` states
the rule ("Marca solo las que trabajaste este periodo. Solo esas entran a la
tabla y a SIAGIE."). Each Competency is a `GCheckRow` on a hairline, 48dp
minimum with 12dp vertical padding: the checkbox (`primary` fill when
checked), `siagieOrdinal` as the `labelSmall` prefix, the name as `bodyLarge`
wrapping to as many lines as it needs; nothing truncates. A row that is
unmarked while `recordedLevelCount` is greater than zero carries the warning
as its `subtitle` ("8 niveles registrados. Quedan guardados y dejan de
exportarse.", the wording the shipped strings carry: the fear to answer is
that unmarking deletes; `recordedLevelCount` counts a Student row the moment
it has an achievement level or an unworked comment, so "niveles" here also
covers rows recorded only as a comment), next to the box that caused it (new: today one WARNING
`GBanner` in the footer aggregates every such Competency; the subtitle in
`GemaAccents.onWarningContainer` text follows the status-ink rule in
`system.md` and is a proposal for the same `core:ui` ticket as the
`GSwitchRow` note, since `GCheckRow` hardcodes `onSurfaceVariant`). The footer is one `bodySmall` line: "`selectedCount` de
N marcadas · cada cambio se guarda solo".

```
+------------------------------------------+
|  <   Competencias trabajadas             |
|      Personal Social · II Bimestre       |
+------------------------------------------+
|  Marca solo las que trabajaste este      |
|  periodo. Solo esas entran a la tabla y  |
|  a SIAGIE.                               |
|  ----------------------------------------|
|  [x] 01  Construye su identidad          |
|  ----------------------------------------|
|  [x] 02  Convive y participa             |
|          democráticamente en la búsqueda |
|          del bien común                  |
|  ----------------------------------------|
|  [ ] 03  Construye interpretaciones      |
|          históricas                      |
|          8 niveles registrados. Quedan   |
|          guardados y dejan de exportarse.|
|  ----------------------------------------|
|  [ ] 04  Gestiona responsablemente el    |
|          espacio y el ambiente           |
|  ----------------------------------------|
|  [x] 05  Gestiona responsablemente los   |
|          recursos económicos             |
|  ----------------------------------------|
|  3 de 5 marcadas · cada cambio se guarda |
|  solo                                    |
+------------------------------------------+
```

```kotlin
data class WorkedCompetenciesUiState(
    val isLoading: Boolean = true,
    val areaName: String = "",
    val periodLabel: String = "",
    val competencies: List<CompetencyToggleRow> = emptyList(),
    val selectedCount: Int = 0,
)

data class CompetencyToggleRow(
    val id: CompetencyId,
    val siagieOrdinal: Int,
    val name: String,
    val isWorked: Boolean,
    val recordedLevelCount: Int,
)
```

Intents: `CompetencyToggled(id: CompetencyId, isWorked: Boolean)`, `BackClicked`.

Effects: `NavigateBack`, `ShowMessage(message: WorkedCompetenciesMessage)`.

Note: unmarking a Competency that already has Period Levels warns but never
deletes; the levels stop being exported and reappear if it is remarked.

Copy changes (for the implementation ticket)

- `worked_competencies_hint`: "Marca solo las competencias que trabajaste.
  Solo esas se exportan." -> "Marca solo las que trabajaste este periodo.
  Solo esas entran a la tabla y a SIAGIE." (names both destinations — the
  Period Levels table and the SIAGIE export — instead of only the export).
- `worked_competencies_marked_count`: "%1$d de %2$d marcada(s)" -> "%1$d de
  %2$d marcada(s) · cada cambio se guarda solo" (the same save-on-toggle
  reassurance AttendanceDay and SectionAreas carry in their footer/subtitle).

---

## 15. PeriodLevels

Mockup: [html](mockups/period-levels.html) (no PNG until Phase 4
regenerates it from the emulator).

Entry: SectionDetail. The grid for one Section x Period x Area.
Primary action: tap a cell. The screen is an Área picker (`GDropdownPicker`)
above the read-only level grid: state (current, incomplete) lives in the
cell's border, never a fill, so the grid itself stays ink on a white ground.

```
+------------------------------------------+
|  <   Niveles - 3ro A          [faltan 12]|
+------------------------------------------+
|  Personal Social v   |  II Bimestre v    |
+------------------------------------------+
|                  |  01    02    05   >>  |
|  ACOSTA RIVERA   | [AD ] [ A ] [   ]     |
|  BAUTISTA QUISPE | [ B ] [ B ] [ C!]     |
|  CCAHUANA MAMANI | [ A ] [   ] [ A ]     |
|  DELGADO HUAMAN  | [C  ] [ C ] [ A ]     |
|  ESPINOZA VEGA   | [ A ] [ A ] [ * ]     |
|                  |                       |
|  <--- desliza para ver mas competencias  |
+------------------------------------------+
|  [AD] [A] [B] [C]  [C!] falta conclusion |
|  [*] comentario    [ ] sin nivel          |
+------------------------------------------+
```

The Student column is pinned; only the competency band scrolls horizontally.
`C!` marks a C without a Descriptive Conclusion — incomplete, not rejected.
`*` marks an Unworked Comment. An empty cell is an empty cell.

Tapping a competency header instead of a cell enters **column mode** (ADR 0015): a bottom
picker offers AD / A / B / C / Sin nivel for the current Student only, records
the tap and advances to the next Student automatically, so one competency for
the whole Section is a straight run of taps with no re-aiming at a grid cell.

```
+------------------------------------------+
|  <   Niveles - 3ro A          [faltan 12]|
+------------------------------------------+
|  Personal Social v   |  II Bimestre v    |
+------------------------------------------+
|  Competencia 02 - 3 de 30                |
|                  |  01   [ 02 ]   05  >> |
|  ACOSTA RIVERA   | [AD ] [ A ] [   ]     |
|  BAUTISTA QUISPE | [ B ] [>B<] [ C!]     |
|  CCAHUANA MAMANI | [ A ] [   ] [ A ]     |
+------------------------------------------+
|  BAUTISTA QUISPE, Jose                   |
|  +-----+ +-----+ +=====+ +-----+ +-----+ |
|  | AD  | |  A  | |  B  | |  C  | | --- | |
|  +-----+ +-----+ +=====+ +-----+ +-----+ |
|             [        Listo        ]      |
+------------------------------------------+
```

`[>B<]` marks the current Student's cell while column mode is open, drawn by
`GLevelChip(isCurrent = true)`. The column-mode sheet titles itself
"COMPETENCIA NN · i DE n" (for example "COMPETENCIA 01 · 4 DE 30" — the
Competency's SIAGIE ordinal, then the current Student's position over the
Section's total), with the caption "Un toque guarda y pasa al siguiente
alumno." underneath the level row, so the Teacher never wonders whether a
tap advances automatically. A single
tap on a level closes that Student's row and reopens the picker for the next
one; *Listo* or picking a level for the last Student exits column mode. A tap
on any other cell while column mode is closed still opens `PeriodLevelSheet`
for that one Student x Competency, Descriptive Conclusion included.

```kotlin
data class PeriodLevelsUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val areas: List<AreaOption> = emptyList(),
    val selectedArea: Area? = null,
    val periods: List<PeriodOption> = emptyList(),
    val selectedPeriodId: PeriodId? = null,
    val columns: List<CompetencyColumn> = emptyList(),
    val rows: List<PeriodLevelRow> = emptyList(),
    val missingCount: Int = 0,
    val isMissingFilterOn: Boolean = false,
    val hasWorkedCompetencies: Boolean = false,
    val sheet: PeriodLevelSheetUiState? = null,
    val columnMode: ColumnModeUiState? = null,
)

data class ColumnModeUiState(
    val competencyId: CompetencyId,
    val currentStudentIndex: Int,
)

data class AreaOption(val area: Area, val name: String)

data class PeriodOption(val id: PeriodId, val label: String, val isCurrent: Boolean)

data class CompetencyColumn(
    val id: CompetencyId,
    val siagieOrdinal: Int,
    val name: String,
)

data class PeriodLevelRow(
    val studentId: StudentId,
    val displayName: String,
    val cells: List<PeriodLevelCell>,
)

data class PeriodLevelCell(
    val competencyId: CompetencyId,
    val achievementLevel: AchievementLevel?,
    val unworkedComment: UnworkedComment?,
    val hasDescriptiveConclusion: Boolean,
    val isIncomplete: Boolean,
)

data class PeriodLevelCellKey(
    val studentId: StudentId,
    val competencyId: CompetencyId,
)
```

Intents (all on `PeriodLevelsUiIntent`): `AreaSelected(area: Area)`,
`PeriodSelected(periodId: PeriodId)`, `CellClicked(key: PeriodLevelCellKey)`,
`MissingFilterToggled`, `EnterColumnMode(competencyId: CompetencyId)`,
`PickLevelForCurrent(level: AchievementLevel?)`, `ExitColumnMode`,
`WorkedCompetenciesClicked`, `BackClicked` — plus the `Sheet*` intents and
`SheetDismissed` listed under PeriodLevelSheet below, which belong to this
same sealed interface.

Effects: `NavigateToWorkedCompetencies(sectionId: SectionId, periodId: PeriodId, area: Area)`,
`NavigateBack`, `ShowMessage(message: PeriodLevelsMessage)`.

---

## 16. PeriodLevelSheet

Mockup: [html](mockups/period-level-sheet.html) (no PNG until Phase 4
regenerates it from the emulator).
A bottom sheet over PeriodLevels for one Student x one Competency.
Every change persists on selection; the sheet closes with *Listo*. When the
selected level is C, the Descriptive Conclusion field renders in error state
(error-colored border, helper text in `colorScheme.error`) instead of a separate banner —
the field itself carries the requirement.

```
+------------------------------------------+
|  BAUTISTA QUISPE, Jose                   |
|  02 Convive y participa democraticamente |
+------------------------------------------+
|  Nivel de logro                          |
|  +-----+ +-----+ +-----+ +-----+ +-----+ |
|  | AD  | |  A  | |  B  | |  C  | | --- | |
|  +-----+ +-----+ +=====+ +-----+ +-----+ |
|                                          |
|  O no evaluada                           |
|  ( ) No se realizaron acciones           |
|  ( ) Evidencia insuficiente              |
|  ( ) Otro                                |
|                                          |
|  Conclusion descriptiva                  |
|  +------------------------------------+  |
|  |                                    |  |
|  +------------------------------------+  |
|  Obligatoria para SIAGIE cuando el       |
|  nivel es C. Puedes guardarla despues.   |
|                                          |
|  EVIDENCIAS DE ESTE PERIODO              |
|  10/06  Lectura en grupo          [ B ]  |
|  22/06  Debate del aula           [ A ]  |
|  05/07  Ficha de convivencia      [ B ]  |
|                                          |
|             [    Listo    ]              |
+------------------------------------------+
```

```kotlin
data class PeriodLevelSheetUiState(
    val studentId: StudentId,
    val competencyId: CompetencyId,
    val studentName: String,
    val competencyLabel: String,
    val achievementLevel: AchievementLevel? = null,
    val unworkedComment: UnworkedComment? = null,
    val descriptiveConclusion: String = "",
    val isConclusionRequiredForExport: Boolean = false,
    val evidence: List<EvidenceRow> = emptyList(),
)

data class EvidenceRow(
    val activityId: ActivityId,
    val activityName: String,
    val date: LocalDate,
    val achievementLevel: AchievementLevel,
)
```

`isConclusionRequiredForExport` is `PeriodLevel.isIncomplete`
(`achievementLevel == C && descriptiveConclusion.isBlank()`), because the
conclusion is optional unless the level is C (`docs/siagie/README.md`). A C
that has a conclusion never renders the error.

Intents (on `PeriodLevelsUiIntent`, shared with PeriodLevels above):
`SheetAchievementLevelSelected(level: AchievementLevel?)`,
`SheetUnworkedCommentSelected(comment: UnworkedComment?)`,
`SheetDescriptiveConclusionChanged(value: String)`, `SheetDismissed` — there
is no separate `DoneClicked`; *Listo* dispatches `SheetDismissed`.

Effects: the sheet has no effect type of its own. A save failure shares the
parent screen's `PeriodLevelsUiEffect.ShowMessage(message:
PeriodLevelsMessage)`; dismissal is the `SheetDismissed` intent above, not a
separate `Dismiss` effect.

Notes:

- Selecting an Achievement Level clears the Unworked Comment and vice versa: a
  Period Level holds exactly one of the two (ADR 0003). The state carries both
  fields because the sheet must render the transition, but the write enforces
  exclusivity in the domain.
- `--- ` in the level row is the explicit "sin nivel" option; clearing a level is
  an action, not a long-press or a swipe.
- `evidence` is read-only. There is no control on this sheet that copies an
  Evidence Level into the Period Level. The section is hidden entirely when
  the list is empty, rather than showing an empty placeholder.

---

## 17. Activities

Mockup: [html](mockups/activities.html) (no PNG until Phase 4 regenerates
it from the emulator).

Entry: SectionDetail. Activities of the current Period, newest first.
Primary action: `GExtendedFab` *Nueva actividad*.

Registro layout: `GTopBar` "Actividades · 3ro A" with the sort rule as
subtitle ("Más recientes primero"). The Period is a `GDropdownPicker` with
the same shell as the Area picker on PeriodLevels. Each Activity is a
`GListItem` on a hairline: the date as a leading `GDateChip` ("5 jul"; new:
the chip renders `dd/MM`, e.g. "22/06", not the abbreviated-month format
shown here), `name` as `titleMedium`, and the subtitle joins
`competencyLabels` with "`evidenceRecordedCount` de `studentCount` con
evidencia"; while evidence is still missing the subtitle reads in
`GemaAccents.onWarningContainer` ink (the status-ink rule in `system.md`),
and a complete Activity reads "M de M" in `onSurfaceVariant` (new: today
the ink is inverted, default while missing and `onPrimaryContainer` when
complete, and the "con evidencia" wording never drops; `GListItem` already
takes `subtitleColor`, so this is a screen-side change, not a `core:ui`
one). The `GExtendedFab` is the one
dark object on the screen.

```
+------------------------------------------+
|  <   Actividades · 3ro A                 |
|      Más recientes primero               |
+------------------------------------------+
|  +------------------------------------+  |
|  | Periodo                            |  |
|  | II Bimestre                      v |  |
|  +------------------------------------+  |
|  ----------------------------------------|
|  5 jul   Ficha de convivencia          > |
|          PPSS 02 · 24 de 30 con evidencia|
|  ----------------------------------------|
|  22 jun  Debate del aula               > |
|          PPSS 01, PPSS 02 · 30 de 30     |
|  ----------------------------------------|
|  10 jun  Lectura en grupo              > |
|          COMU 01 · 18 de 30 con evidencia|
|  ----------------------------------------|
|                                          |
|                      [+ Nueva actividad] |
+------------------------------------------+
```

```kotlin
data class ActivitiesUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val periods: List<PeriodOption> = emptyList(),
    val selectedPeriodId: PeriodId? = null,
    val activities: List<ActivityRow> = emptyList(),
) {
    val periodLabel: String
        get() = periods.find { it.id == selectedPeriodId }?.label.orEmpty()
}

data class PeriodOption(
    val id: PeriodId,
    val label: String,
    val isCurrent: Boolean,
)

data class ActivityRow(
    val id: ActivityId,
    val name: String,
    val date: LocalDate,
    val competencyLabels: List<String>,
    val evidenceRecordedCount: Int,
    val studentCount: Int,
)
```

Intents: `PeriodSelected(id: PeriodId)`, `ActivityClicked(id: ActivityId)`,
`AddActivityClicked`, `BackClicked`.

Effects: `NavigateToActivityEvidence(activityId: ActivityId)`,
`NavigateToActivityForm(sectionId: SectionId, activityId: ActivityId?)`,
`NavigateBack`.

Copy changes (for the implementation ticket)

- `activities_list_subtitle`: "Las más nuevas primero" -> "Más recientes primero" (matches the Registro wording chosen for the subtitle)

---

## 18. ActivityForm

Mockup: [html](mockups/activity-form.html) (no PNG until Phase 4
regenerates it from the emulator).

Entry: Activities. Primary action: *Guardar*.

Registro layout: `GTopBar` "Nueva actividad" or "Editar actividad" with the
Section as subtitle, becoming "Sección · Periodo" once `resolvedPeriodLabel`
is known. `name` is a `GTextField`; `date` is a `GDateField` followed by a
separate `GText` line carrying `resolvedPeriodLabel` ("Cae en el III
Bimestre."), because `GDateField` has no supporting-line parameter of its own
beyond its error text. While `periodChangeFromLabel` is non-null a WARNING
`GBanner` sits directly under the date and names the source and target
Period, stating the Activity's evidence moves with it. Any non-blank
`dateError` renders through `GDateField`'s own `errorText` slot.
"COMPETENCIAS TRABAJADAS" is an eyebrow; each `CompetencyGroup` is its
`areaName` as a plain `bodySmall` weight-500 label (`GTextStyle.BODY_SMALL_EMPHASIS`)
on the screen ground, followed by `GCheckRow`s with the SIAGIE ordinal as
prefix. In edit mode (`canDelete`) a "ZONA DE RIESGO" eyebrow groups a
DESTRUCTIVE `GButton` "Eliminar actividad" with one `bodySmall` helper
("Borra también sus evidencias. Te preguntamos antes."), the same anatomy as
SectionForm; create mode is the same screen without that group. *Guardar* is
the `bottomAction` PRIMARY `GButton`.

```
+------------------------------------------+
|  <   Editar actividad                    |
|      3ro A                               |
+------------------------------------------+
|  +------------------------------------+  |
|  | Nombre                             |  |
|  | Debate del aula                    |  |
|  +------------------------------------+  |
|  +------------------------------------+  |
|  | Fecha                          [c] |  |
|  | 10/08/2026                         |  |
|  +------------------------------------+  |
|    Cae en el III Bimestre.               |
|  +--------------------------------------+|
|  | Con esta fecha la actividad pasa del ||
|  | II al III Bimestre. Sus evidencias   ||
|  | se mueven con ella.                  ||
|  +--------------------------------------+|
|                                          |
|  COMPETENCIAS TRABAJADAS                 |
|  Personal Social                         |
|  ----------------------------------------|
|  [x] 01  Construye su identidad          |
|  ----------------------------------------|
|  [x] 02  Convive y participa             |
|          democráticamente en la búsqueda |
|          del bien común                  |
|  Comunicación                            |
|  ----------------------------------------|
|  [ ] 01  Se comunica oralmente en su     |
|          lengua materna                  |
|  ----------------------------------------|
|                                          |
|  ZONA DE RIESGO                          |
|  [       Eliminar actividad          ]   |
|  Borra también sus evidencias. Te        |
|  preguntamos antes.                      |
+------------------------------------------+
|             [   Guardar   ]              |
+------------------------------------------+
```

```kotlin
data class ActivityFormUiState(
    val isLoading: Boolean = true,
    val activityId: ActivityId? = null,
    val grade: Grade? = null,
    val sectionName: String = "",
    val name: String = "",
    val date: LocalDate? = null,
    val dateError: ActivityFormMessage? = null,
    val resolvedPeriodLabel: String? = null,
    val periodChangeFromLabel: String? = null,
    val competencyGroups: List<CompetencyGroup> = emptyList(),
    val selectedCompetencyIds: Set<CompetencyId> = emptySet(),
    val isDeleteConfirmVisible: Boolean = false,
) {
    val canSave: Boolean
        get() = name.isNotBlank() && date != null && resolvedPeriodLabel != null && selectedCompetencyIds.isNotEmpty()

    val canDelete: Boolean
        get() = activityId != null
}

data class CompetencyGroup(
    val areaName: String,
    val competencies: List<CompetencyToggleRow>,
)

data class CompetencyToggleRow(
    val id: CompetencyId,
    val siagieOrdinal: Int,
    val name: String,
)
```

Intents: `NameChanged(value: String)`, `DateChanged(value: LocalDate)`,
`CompetencyToggled(id: CompetencyId, isSelected: Boolean)`, `SaveClicked`,
`DeleteClicked`, `DeleteConfirmed`, `DeleteDismissed`, `BackClicked`.

Effects: `NavigateToActivityEvidence(activityId: ActivityId)`, `NavigateBack`,
`ShowMessage(message: ActivityFormMessage)`.

Notes:

- `resolvedPeriodLabel` comes from the date (US 35); the Period is never picked.
- `periodChangeFromLabel` holds the Activity's original Period label while
  editing when the new date moves it to a different Period, and is `null`
  otherwise (create mode, same Period, or no resolved Period); `canSave` and
  `canDelete` are computed from the rest of the state, not stored fields.
- The competency list is restricted to Worked Competencies of the resolved
  Period and active Areas.
- `ActivityFormMessage` (`dateError` and `ShowMessage`) is `OUTSIDE_PERIODS` or
  `SAVE_FAILED`.

Copy changes (for the implementation ticket)

- `activity_form_delete_eyebrow`: "ZONA DE RIESGO" eyebrow label above the delete button
- `activity_form_delete_helper`: "Borra también sus evidencias. Te preguntamos antes." helper line under *Eliminar actividad*
- `activity_form_period_change_warning`: "Con esta fecha la actividad pasa del %1$s al %2$s. Sus evidencias se mueven con ella." (names the specific Periods and evidence, matches the wireframe and mockup)

---

## 19. ActivityEvidence

Mockup: [html](mockups/activity-evidence.html) (no PNG until Phase 4
regenerates it from the emulator).

Entry: Activities. One Evidence Level per Student per Competency of this
Activity. Saves on tap.

Registro layout: `GTopBar` with `activityName`, the subtitle
"`activityDateLabel` · `periodLabel`" (new: the `activity_evidence_subtitle`
format string takes only these two parameters; "cada toque se guarda solo"
is not part of it today), and the overflow (Editar actividad) as its one
action. The Competency is a `GDropdownPicker` ("PPSS 01 · Construye su
identidad"). A summary strip mirrors AttendanceDay: `recordedCount` as
`NUMERAL` with "de `totalCount` con evidencia", and the untouched count as
a `bodySmall` line in `GemaAccents.onWarningContainer` text ("6 sin marcar")
(new: today the strip is a plain `Row`, not a `surfaceContainerLow`
container; it shows only "`recordedCount`/`totalCount`" in
`LABEL_LARGE_EMPHASIS`/`onSurfaceVariant`, with no separate untouched
count). Each Student is a hairline row with the name as `titleMedium` and a
`GLevelPicker` of five 52dp chips (AD, A, B, C, —). A graded row selects its
letter with the `primaryContainer` fill and 2dp `primary` border, letter in
`onSurface` ink. An explicit `NoEvidence` mark selects the dash chip with the
`surfaceContainerHigh` fill and the plain `outline` border, never the
`primary` border, because "no evidence" is a state, not an achievement (new:
`GLevelPicker` selects every option, dash included, with the same
`primaryContainer` fill and `primary` border today). An untouched row
(`mark == null`) draws its chips with a dashed `outline` border and a
trailing "sin marcar" label, the same treatment as an unmarked attendance row
(new: the whole row gets a `GemaAccents.warningContainer` background wash,
the trailing label reads "sin evidencia" from
`activity_evidence_untouched_label`, and the chip borders are the same
solid `outline` stroke as any unselected chip — `GSegmentedPicker` does not
support a dashed border).

```
+------------------------------------------+
|  <   Debate del aula                [.:.]|
|      22 jun · II Bimestre · cada toque   |
|      se guarda solo                      |
+------------------------------------------+
|  +------------------------------------+  |
|  | Competencia                        |  |
|  | PPSS 01 · Construye su identidad v |  |
|  +------------------------------------+  |
|  +------------------------------------+  |
|  | 24 de 30 con evidencia  6 sin marcar|  |
|  +------------------------------------+  |
|  ----------------------------------------|
|  APAZA CONDORI, YESENIA                  |
|  +----+ +----+ +----+ +----+ +----+      |
|  |[AD]| | A  | | B  | | C  | | —  |      |
|  +----+ +----+ +----+ +----+ +----+      |
|  ----------------------------------------|
|  HUANCA RÍOS, DIEGO            sin marcar |
|  +----+ +----+ +----+ +----+ +----+      |
|  : AD : : A  : : B  : : C  : : —  :      |
|  +----+ +----+ +----+ +----+ +----+      |
|  ----------------------------------------|
|  MAMANI TORRES, LUIS ALBERTO             |
|  +----+ +----+ +----+ +----+ +----+      |
|  | AD | | A  | | B  | | C  | |(—) |      |
|  +----+ +----+ +----+ +----+ +----+      |
+------------------------------------------+
```

With several Competencies the screen keeps a competency selector at the top
rather than widening the row: one Competency at a time, one tap per Student.

```kotlin
data class ActivityEvidenceUiState(
    val isLoading: Boolean = true,
    val sectionId: SectionId? = null,
    val activityName: String = "",
    val activityDateLabel: String = "",
    val periodLabel: String = "",
    val competencies: List<CompetencyColumn> = emptyList(),
    val selectedCompetencyId: CompetencyId? = null,
    val recordedCount: Int = 0,
    val totalCount: Int = 0,
    val rows: List<EvidenceLevelRow> = emptyList(),
)

data class CompetencyColumn(
    val id: CompetencyId,
    val label: String,
)

data class EvidenceLevelRow(
    val studentId: StudentId,
    val displayName: String,
    val mark: EvidenceMark?,
)
```

`EvidenceMark` (`core:domain`) is a sealed interface: `EvidenceMark.Level(achievementLevel: AchievementLevel)`
for a graded row and `EvidenceMark.NoEvidence` for an explicit "no evidence" mark. `mark == null` means the
Student is untouched — distinct from an explicit `NoEvidence` mark, which is what tapping the "—" chip
records.

Intents: `CompetencySelected(id: CompetencyId)`,
`LevelSelected(studentId: StudentId, mark: EvidenceMark?)`,
`EditActivityClicked`, `BackClicked`.

Effects: `NavigateToActivityForm(sectionId: SectionId, activityId: ActivityId?)`,
`NavigateBack`, `ShowMessage(message: ActivityEvidenceMessage)`.

`ActivityEvidenceMessage` is `RECORD_FAILED`.

Copy changes (for the implementation ticket)

- `activity_evidence_untouched_label`: "sin evidencia" -> "sin marcar" (matches the untouched-row wording the Registro layout and wireframe use)

---

## 20. Export

Mockup: [html](mockups/export.html) · no template
[html](mockups/export-no-template.html) (no PNG until Phase 4 regenerates
them from the emulator).

Entry: SectionDetail. All three outputs for one Section and Period.

Registro layout: `GTopBar` "Entregar · 3ro A" with `periodLabel` as its
subtitle. The three outputs are eyebrow groups on the screen ground
separated by hairlines and 24dp, not cards (principle 6, fewer cards) (new:
today each group is still a `GCard` with a hairline border, and its title
is sentence-case `TITLE_MEDIUM` text — "Notas para SIAGIE" — not an
uppercase eyebrow): "NOTAS PARA SIAGIE", "ASISTENCIA PARA SIAGIE", "RESUMEN
PARA IMPRIMIR". The grades group shows `templateFileName` with a file glyph
in `onSurfaceVariant` (new: it is plain `BODY_LARGE` text today, with no
glyph); when `gradesExportState` is `Blocked` the gap count reads as
`bodyLarge` weight 500 in `GemaAccents.onWarningContainer` text (see the
text-only note in `system.md`) above one `GListItem` per `ExportGapRow`
(`studentName`, `competencyLabel` as subtitle, chevron) (new: the gap count
renders inside a WARNING `GBanner` today, not as plain colored text), and
*Generar archivo* is the one PRIMARY `GButton` on the screen, disabled until
`Ready`. `templateMismatch` renders as a WARNING `GBanner` in the same
group. The attendance group shows "`attendanceMonth` · `attendanceDayCount`
días registrados" and a SECONDARY *Exportar el mes*, which first opens a
SIAGIE attendance-template picker (`OpenAttendanceTemplatePicker` /
`AttendanceTemplatePicked`) before exporting; the Resumen group has
one `bodyLarge` line and two SECONDARY buttons, PDF and CSV, side by side
(new: the descriptive line under "Resumen para imprimir" is not rendered
today; the card holds only the title and the two buttons).
`activeExport` sets `isBusy` on the running button and disables the rest.
The share note stays as the `bodySmall` footer.

```
+------------------------------------------+
|  <   Entregar · 3ro A                    |
|      II Bimestre                         |
+------------------------------------------+
|  NOTAS PARA SIAGIE                       |
|  [f] 3 Primaria EBR.xlsx                 |
|  2 conclusiones descriptivas faltan      |
|  ----------------------------------------|
|  CCAHUANA FLORES, MARÍA                > |
|  Personal Social · 02                    |
|  ----------------------------------------|
|  HUANCA RÍOS, DIEGO                    > |
|  Comunicación · 01                       |
|  ----------------------------------------|
|  [    Generar archivo (disabled)     ]   |
|                                          |
|  ----------------------------------------|
|  ASISTENCIA PARA SIAGIE                  |
|  setiembre 2026 · 20 días registrados    |
|  [         Exportar el mes           ]   |
|                                          |
|  ----------------------------------------|
|  RESUMEN PARA IMPRIMIR                   |
|  Una tabla por área, con los niveles     |
|  del periodo.                            |
|  [      PDF      ]  [      CSV      ]    |
|                                          |
|  El archivo conserva su nombre original  |
|  y se comparte por WhatsApp, Bluetooth   |
|  o USB.                                  |
+------------------------------------------+
```

The period under the title is a static, read-only subtitle — not an editable
dropdown — matching every other section detail screen in the app (ADR-less
fix, issue #121).

When there are no blockers, the grades card drops the gap list and its
*Generar archivo* button enables:

```
|  | Notas para SIAGIE                    ||
|  | 3 Primaria EBR.xlsx                  ||
|  |          [  Generar archivo  ]       ||
```

When no Template is stored (`gradesExportState` is `Unavailable`), the
grades group collapses to an INFO `GBanner` whose LINK action is *Importar
plantilla* (`ImportTemplateClicked`), the group order is unchanged, and the
Resumen group's PDF button takes the PRIMARY slot: that is how the screen
promotes PDF/CSV (US 53) without a second primary (new: today `Unavailable`
renders plain text plus a SECONDARY *Importar plantilla* `GButton`, not a
`GBanner` — though `GBanner` already supports a LINK `actionStyle`, so
wiring it is a screen-side change — and the Resumen card always renders
both PDF and CSV as SECONDARY regardless of `gradesExportState`; it has no
promotion logic yet).

```
|  NOTAS PARA SIAGIE                       |
|  +--------------------------------------+|
|  | Esta sección no tiene una plantilla  ||
|  | SIAGIE. Impórtala desde Alumnos para ||
|  | generar las notas.                   ||
|  | Importar plantilla                 > ||
|  +--------------------------------------+|
|  ...                                     |
|  RESUMEN PARA IMPRIMIR                   |
|  [  PDF (primary)  ]  [      CSV      ]  |
```

```kotlin
data class ExportUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val periodId: PeriodId? = null,
    val periodLabel: String = "",
    val templateFileName: String? = null,
    val gradesExportState: GradesExportUiState = GradesExportUiState.Unavailable,
    val templateMismatch: TemplateMismatchUi? = null,
    val attendanceMonth: YearMonth? = null,
    val attendanceDayCount: Int = 0,
    val activeExport: ActiveExport? = null,
)

sealed interface GradesExportUiState {
    data object Unavailable : GradesExportUiState
    data object Ready : GradesExportUiState
    data class Blocked(val gaps: List<ExportGapRow>) : GradesExportUiState
}

data class ExportGapRow(
    val studentId: StudentId,
    val studentName: String,
    val competencyId: CompetencyId,
    val competencyLabel: String,
)

data class TemplateMismatchUi(
    val areaNames: List<String>,
    val studentNames: List<String>,
    val competencyLabels: List<String>,
)
```

`activeExport: ActiveExport?` (ticket #14) is `GRADES`, `ATTENDANCE`,
`SUMMARY_CSV` or `SUMMARY_PDF`, or `null` when nothing is exporting; the
screen derives each button's busy/enabled state from it, and only one
export runs at a time. The attendance card reads `attendanceMonth` and
`attendanceDayCount` directly off `ExportUiState`.

Intents: `ExportGradesClicked`, `GapRowClicked(row: ExportGapRow)`,
`ImportTemplateClicked`, `ExportAttendanceClicked`,
`AttendanceTemplatePicked(uri: String)`, `ExportSummaryCsvClicked`,
`ExportSummaryPdfClicked`, `BackClicked`.

Effects: `ShareFile(path: String, mimeType: String)`,
`OpenAttendanceTemplatePicker(mimeTypes: List<String>)`,
`NavigateToPeriodLevelCell(sectionId: SectionId, studentId: StudentId, competencyId: CompetencyId)`,
`NavigateToStudents(sectionId: SectionId)`, `NavigateBack`,
`ShowMessage(message: ExportMessage)`.

A gap row opens Period Levels on that exact cell: the route carries `studentId`
and `competencyId`, and Period Levels selects the Competency's Area and opens
its `PeriodLevelSheet` as soon as the grid has loaded. Without those arguments
the grid opens as before, on the current Period and the first active Area.

`templateMismatch` is filled when the stored Template cannot hold everything the
Period recorded — an active Area with no sheet, a Worked Competency with no
column in that sheet, or a Student the Template does not carry. It reads as a warning banner in the grades card and no file is
written (ADR 0018).

The Resumen card's PDF and CSV buttons read `activeExport` to show which one
is busy; every action on the screen — Grades' *Generar archivo*, Attendance's
*Exportar*, and both Resumen buttons — disables while any export is in
flight (`activeExport != null`), not only its own sibling. Its
`ExportMessage.EXPORT_FAILED` on failure is the same message the grades card
uses; `ExportMessage` also defines `ATTENDANCE_EXPORT_FAILED` for the
attendance card and `EXPORT_UNAVAILABLE` for the missing-Template state — the
screen does not share one failure message across every card. Each output is one table
per active Area (a title row, then
"Estudiante" plus one column per Worked Competency), not one flat table
spanning every Area — see ADR 0020.

Note: screen title is "Entregar · {sectionTitle}" (`export_title`), because
"Entregar" is the Teacher's goal, not the file format. Every blocking gap now lists as a tap-through row inside the
grades card itself — no separate bottom sheet — and `ExportGradesClicked`
(now labelled *Generar archivo*) is enabled only when `gradesExportState` is
`Ready`. `PeriodLevel.isIncomplete` only blocks a C level with a blank
Descriptive Conclusion, and `GetGradesExportPlanUseCase` skips unrecorded
cells entirely, so a missing level never blocks the export.

Copy changes (for the implementation ticket)

- `export_grades_blocked`: "Faltan %1$d conclusiones descriptivas" -> "%1$d conclusiones descriptivas faltan" (matches the wireframe and mockup word order)
- `export_grades_unavailable`: "Esta sección no tiene una plantilla SIAGIE importada. Importa una desde Alumnos, o usa el resumen PDF/CSV." -> "Esta sección no tiene una plantilla SIAGIE. Impórtala desde Alumnos para generar las notas." (matches the export-no-template mockup)
- `export_attendance_subtitle`: "%1$s · %2$d días" -> "%1$s · %2$d días registrados" (matches the wireframe and mockup)
- `export_attendance_export`: "Exportar" -> "Exportar el mes" (matches the wireframe and mockup)
- New: "Una tabla por área, con los niveles del periodo." description line under "Resumen para imprimir" (no string key yet; the line is not rendered today)

---

## 21. Backup

Mockup: [html](mockups/backup.html) · restore confirmation
[html](mockups/backup-restore-confirm.html) (no PNG until Phase 4
regenerates them from the emulator).

Entry: Home overflow or the reminder banner.

Registro layout: `GTopBar` "Respaldo" with one line of subtitle saying what
the file holds. The last-backup line is a strip with the "ÚLTIMO RESPALDO"
eyebrow and the days-and-date label as `NUMERAL` ("hace 12 días · 29 ago")
(new: the label is a computed string, not a stored `lastBackupLabel` field,
and it renders as `TITLE_MEDIUM`, not `NUMERAL`): while the reminder is due
it is the ERROR strip (`errorContainer`, leading dot), the same signal as
the Home banner (new: the leading element is a `Warning` triangle icon, not
a dot); otherwise it is the INFO strip on `surfaceContainerLow` (new: today
it is a bordered `GCard` on plain `surface`, not a borderless INFO strip on
`surfaceContainerLow`). *Crear respaldo* is the one PRIMARY `GButton`
(`isCreating` sets `isBusy`), with a `bodySmall` helper naming the share
sheet. A "RESTAURAR" eyebrow groups the SECONDARY *Elegir archivo .gema* and
a WARNING `GBanner` ("Restaurar reemplaza todo lo que hay en este teléfono y
reinicia la app. No se puede deshacer."). A "RECORDATORIO" eyebrow groups a
short numeric `GTextField` (label "Cada", 96dp wide) beside its unit "días
sin respaldar" and a `bodySmall` helper; `reminderThresholdError` renders as
the field's error text (new: today one "RECORDARME CADA" label stands in
for both the eyebrow and the field label — the `GTextField` itself has no
label — the field is 112dp wide, the unit text is just "días", there is no
helper line below it, and the error text comes from `isReminderThresholdInvalid`
gating a fixed "Elige entre X y Y días" string, not a stored error message).

```
+------------------------------------------+
|  <   Respaldo                            |
|      Todo lo de este teléfono en un      |
|      archivo .gema                       |
+------------------------------------------+
|  +--------------------------------------+|
|  | ÚLTIMO RESPALDO                    • ||
|  | hace 12 días · 29 ago                ||
|  +--------------------------------------+|
|  [          Crear respaldo           ]   |
|  Se crea el archivo y se abre el menú    |
|  para compartirlo: WhatsApp, Drive,      |
|  Bluetooth o USB.                        |
|  ----------------------------------------|
|  RESTAURAR                               |
|  [       Elegir archivo .gema        ]   |
|  +--------------------------------------+|
|  | Restaurar reemplaza todo lo que hay  ||
|  | en este teléfono y reinicia la app.  ||
|  | No se puede deshacer.                ||
|  +--------------------------------------+|
|  ----------------------------------------|
|  RECORDATORIO                            |
|  +--------+                              |
|  | Cada   |  días sin respaldar          |
|  | 7      |                              |
|  +--------+                              |
|  Pasado ese tiempo, Inicio muestra el    |
|  aviso de respaldo.                      |
+------------------------------------------+
```

The restore confirmation is a `GDialog(isDestructive = true)` with
`fileName` as its subtitle, one statement, the `RestoreConfirmation` counts
as `NUMERAL` hairline rows (the same anatomy as the Section delete dialog),
and a `bodySmall` line announcing the restart so it never reads as a crash
(new: `GDialog` has no `subtitle` parameter, so `fileName` renders inside
the first body sentence instead — "`fileName` reemplazará los datos de este
teléfono."; the school-year count is one plain `BODY_LARGE` line, not a
`NUMERAL` hairline row, and there is no `alumnos` row at all, because
`RestoreConfirmation` carries no student count field today).

```
+------------------------------------------+
|  ¿Restaurar este respaldo?               |
|  gema-2026-08-29.gema                    |
|  Se reemplaza todo lo que hay en este    |
|  teléfono:                               |
|  ----------------------------------------|
|     1  año escolar                       |
|  ----------------------------------------|
|    57  alumnos                           |
|  ----------------------------------------|
|  La app se reinicia al terminar. No es   |
|  un error.                               |
|                    Cancelar   Restaurar  |
+------------------------------------------+
```

```kotlin
data class BackupUiState(
    val isLoading: Boolean = true,
    val lastBackupDate: LocalDate? = null,
    val daysSinceLastBackup: Int? = null,
    val reminderThresholdDays: Int = DEFAULT_REMINDER_THRESHOLD_DAYS,
    val reminderThresholdInput: String = DEFAULT_REMINDER_THRESHOLD_DAYS.toString(),
    val isReminderThresholdInvalid: Boolean = false,
    val isBackupOverdue: Boolean = false,
    val isCreating: Boolean = false,
    val restoreConfirmation: RestoreConfirmation? = null,
    val isRestoring: Boolean = false,
)

data class RestoreConfirmation(
    val uri: String,
    val fileName: String,
    val currentSchoolYearCount: Int,
    val currentStudentCount: Int,
)
```

`DEFAULT_REMINDER_THRESHOLD_DAYS` (`core:domain`) is `7`. `currentStudentCount`
renders the "57 alumnos" row the wireframe shows and counts every Student on
the device, active and withdrawn, since a restore replaces both.

Intents: `CreateBackupClicked`, `ChooseRestoreFileClicked`,
`RestoreFilePicked(uri: String)`, `RestoreConfirmed`, `RestoreDismissed`,
`ReminderThresholdChanged(value: String)`, `BackClicked`.

Effects: `ShareFile(path: String, mimeType: String)`,
`OpenDocumentPicker(mimeTypes: List<String>)`, `RestartApp`, `NavigateBack`,
`ShowMessage(message: BackupMessage)`.

`BackupMessage` is `BACKUP_CREATED`, `BACKUP_FAILED`, `FILE_IS_NOT_A_BACKUP`,
`BACKUP_FROM_A_NEWER_APP`, `RESTORE_FAILED` or `MANUAL_RESTART_REQUIRED`.

Note: `isCreating` and `isRestoring` are the only justified progress indicators
in the app; copying a database file is real local work with a visible duration,
unlike a read.

Copy changes (for the implementation ticket)

- `backup_subtitle`: "Todo vive en este teléfono" -> "Todo lo de este teléfono en un archivo .gema" (matches the wireframe and mockup)
- `backup_create_hint`: "Se crea un archivo .gema y se abre el menú para compartir." -> "Se crea el archivo y se abre el menú para compartirlo: WhatsApp, Drive, Bluetooth o USB." (matches the wireframe and mockup)
- `backup_restore_warning`: "Restaurar reemplaza todo lo que hay en este teléfono y reinicia la app." -> "Restaurar reemplaza todo lo que hay en este teléfono y reinicia la app. No se puede deshacer." (shipped in #204; matches `feature/backup/src/main/res/values/strings.xml`)
- `backup_reminder_label`: "RECORDARME CADA" -> "RECORDATORIO" eyebrow, plus a "Cada" label on the `GTextField` itself (matches the wireframe and mockup, which show separate strings)
- `backup_reminder_days`: "días" -> "días sin respaldar" (matches the wireframe and mockup)
- New: "Pasado ese tiempo, Inicio muestra el aviso de respaldo." helper line under the reminder field (no string key yet; the line is not rendered today)
- New: "La app se reinicia al terminar. No es un error." line in the restore confirmation dialog (no string key yet; the dialog carries no such line today)

---

## Open design questions for the product owner

1. **Spanish period vocabulary.** Are the Periods labelled "I Bimestre" /
   "Primer Bimestre" / "Bimestre 1"? This appears on Home, Periods,
   PeriodLevels, Activities and Export, so it should be decided once.
2. **Student display name.** SIAGIE stores one uppercase string, surnames first.
   Do we store and display exactly that, or split into surnames and given names
   for nicer rendering and sorting? Splitting is lossy and risks breaking the
   Export round-trip; keeping one field makes surname ordering a string sort.
   This blocks US 23 and ticket #6.
3. **Multiple Sections per grid.** A multigrade Teacher has several Sections.
   Should Attendance and Period Levels offer a Section switcher inside the
   screen, or is going back to Home acceptable? The wireframes assume the
   latter.
4. **Attendance for a non-school day.** Nothing prevents opening a Saturday or a
   holiday. Do we block weekends, warn, or allow silently? The current design
   allows silently, since rural schedules vary and the SIAGIE attendance
   template drives the real calendar.
5. **Withdrawal date vs. attendance already recorded.** If a Student is withdrawn
   on the 4th but has attendance recorded on the 10th, do we delete those rows,
   keep them hidden, or refuse the withdrawal date? The design keeps them and
   hides the Student; the Export behaviour for that case is undefined.
6. **Unworked Comment wording.** `docs/siagie/README.md` records the meanings
   ("no actions carried out", "not enough evidence", "other") but the exact
   Spanish strings SIAGIE shows in its dropdown are not confirmed. The sheet
   shows our wording; confirm against a real file before ticket #12.
7. **Descriptive Conclusion length.** Does SIAGIE cap it? If it does, the field
   needs a counter and the Export needs a truncation rule.
8. **Export period scope.** Does a SIAGIE grades export write one Period or every
   Period in the file? The current design exports the selected Period only;
   confirm the template has per-period columns or per-period files.
9. **PDF/CSV as a replacement.** For a Section with no Template, is the PDF/CSV
   summary enough for the school, or does the Teacher still need the numbers in
   SIAGIE order with the SIAGIE headers? That changes the CSV column layout.
10. **Backup reminder placement.** Home is the only place it appears. Should it
    also block or warn on Export, which is the moment a Teacher is most likely
    to be about to reinstall or change phone?
11. **Backup restore across app versions.** Restoring a `.gema` from an older
    schema into a newer app is a real scenario on a shared phone. Is a version
    check with a clear refusal acceptable for v1, or must it migrate?
12. **Areas hidden after data exists.** Hiding an Area with recorded Period
    Levels currently keeps them and removes them from Export. Confirm that
    matches what a Teacher expects, or whether hiding should be refused.
