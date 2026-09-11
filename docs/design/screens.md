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
`SectionId`, `StudentId`, `AreaId`, `CompetencyId`, `ActivityId`, `Grade`,
`PeriodKind`, `AchievementLevel`, `UnworkedComment`, `AttendanceStatus`.
Dates are `kotlinx.datetime.LocalDate` / `YearMonth`.

`isLoading: Boolean` appears on every state because the MVI template defines it;
on this app it is true for roughly one frame, so no wireframe draws it.

---

## 1. SetupYear

First screen of a cold start with no School Year. Entry: app launch.
Shows the year, its kind and its Periods. Primary action: *Continuar*.

```
+------------------------------------------+
|  Configura tu ano escolar                |
|  Paso 1 de 2                             |
+------------------------------------------+
|  Calculamos las fechas de tus periodos   |
|  por ti. Puedes ajustar cualquiera.      |
|                                          |
|  Ano                                     |
|  +------------------------------------+  |
|  | 2026                               |  |
|  +------------------------------------+  |
|                                          |
|  Inicio              Fin                 |
|  +---------------+  +----------------+   |
|  | 01/03/2026    |  | 20/12/2026     |   |
|  +---------------+  +----------------+   |
|                                          |
|  Como evalua tu escuela?                 |
|  +------------------+-----------------+  |
|  |  Bimestres (4)   |  Trimestres (3) |  |
|  +------------------+-----------------+  |
|                                          |
|  Periodos                                |
|  +------------------------------------+  |
|  | I    01/03/2026  -  15/05/2026   > |  |
|  | II   18/05/2026  -  31/07/2026   > |  |
|  | III  10/08/2026  -  16/10/2026   > |  |
|  | IV   19/10/2026  -  20/12/2026   > |  |
|  +------------------------------------+  |
+------------------------------------------+
|            [    Continuar    ]           |
+------------------------------------------+
```

Tapping a Period row opens a small editor over this screen:

```
+------------------------------------------+
|  Editar periodo                          |
|  I Bimestre                              |
+------------------------------------------+
|  Inicio              Fin                 |
|  +---------------+  +----------------+   |
|  | 01/03/2026    |  | 15/05/2026     |   |
|  +---------------+  +----------------+   |
|                                          |
|          [ Cancelar ]  [ Guardar ]       |
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
    val yearLabelError: String? = null,
    val dateRangeError: String? = null,
    val canContinue: Boolean = false,
)

data class PeriodDraftRow(
    val ordinal: Int,
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val error: String?,
)

data class PeriodEditorState(
    val ordinal: Int,
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val error: String?,
)
```

Intents: `YearLabelChanged(value: String)`, `StartDateChanged(value: LocalDate)`,
`EndDateChanged(value: LocalDate)`, `PeriodKindSelected(kind: PeriodKind)`,
`PeriodClicked(ordinal: Int)`, `EditorStartDateChanged(value: LocalDate)`,
`EditorEndDateChanged(value: LocalDate)`, `EditorConfirmed`, `EditorDismissed`,
`ContinueClicked`, `BackClicked`.

Effects: `NavigateToSetupSection(draft: SchoolYearDraft)`, `NavigateBack`.

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

---

## 2. SetupSection

Entry: from SetupYear, or from `SchoolYears` when creating a later year.
Shows Grade, Section name and the Area default. Primary action: *Terminar*.

```
+------------------------------------------+
|  Tu primera seccion                      |
|  Paso 2 de 2                             |
+------------------------------------------+
|  Grado                                   |
|  +----+ +----+ +----+ +----+ +----+ +--+ |
|  | 1  | | 2  | | 3  | | 4  | | 5  | |6 | |
|  +----+ +----+ +----+ +----+ +----+ +--+ |
|                                          |
|  Nombre de la seccion                    |
|  +------------------------------------+  |
|  | A                                  |  |
|  +------------------------------------+  |
|                                          |
|  +--------------------------------------+|
|  | i  Todas las areas quedan activas.   ||
|  |    No dicto todas las areas  >       ||
|  +--------------------------------------+|
|                                          |
|  Si ensenas en aula multigrado, crea     |
|  una seccion por grado.                  |
+------------------------------------------+
|            [    Terminar     ]           |
+------------------------------------------+
```

```kotlin
data class SetupSectionUiState(
    val isLoading: Boolean = true,
    val grade: Grade? = null,
    val sectionName: String = "",
    val sectionNameError: String? = null,
    val canFinish: Boolean = false,
    val isSaving: Boolean = false,
)
```

Intents: `GradeSelected(grade: Grade)`, `SectionNameChanged(value: String)`,
`AreaSelectionClicked`, `FinishClicked`, `BackClicked`.

Effects: `NavigateToHome`, `NavigateToSectionAreas(sectionId: SectionId)`,
`NavigateBack`, `ShowMessage(text: String)`.

---

## 3. Home

Entry: launch with an existing School Year, or up from any Section.
Shows the active year, the current Period, the Backup reminder and the Sections.
Primary action: FAB *Nueva seccion*.

```
+------------------------------------------+
|  Gema                    2026 v      [=] |
+------------------------------------------+
|  +--------------------------------------+|
|  | !  Ultimo respaldo hace 12 dias      ||
|  |    Respaldar ahora  >                ||
|  +--------------------------------------+|
|                                          |
|  II Bimestre - faltan 24 dias            |
|                                          |
|  SECCIONES                               |
|  +--------------------------------------+|
|  | 3ro A                     30 alumnos ||
|  | Asistencia de hoy: sin tomar         ||
|  |      [ Tomar asistencia de hoy ]     ||
|  +--------------------------------------+|
|  | 4to B                     27 alumnos ||
|  | Asistencia de hoy: 25 presentes      ||
|  |      [ Tomar asistencia de hoy ]     ||
|  +--------------------------------------+|
|                                          |
|                                          |
|                                  (  +  ) |
+------------------------------------------+
```

```kotlin
data class HomeUiState(
    val isLoading: Boolean = true,
    val schoolYearLabel: String = "",
    val currentPeriodLabel: String? = null,
    val daysLeftInPeriod: Int? = null,
    val backupReminder: BackupReminder? = null,
    val sections: List<SectionRow> = emptyList(),
)

data class SectionRow(
    val id: SectionId,
    val title: String,
    val studentCount: Int,
    val attendanceSummary: String,
)

data class BackupReminder(
    val daysSinceLastBackup: Int,
    val hasEverBackedUp: Boolean,
)
```

Intents: `SectionClicked(id: SectionId)`, `TakeAttendanceClicked(id: SectionId)`,
`AddSectionClicked`, `SchoolYearSwitcherClicked`, `BackupReminderClicked`,
`SettingsClicked`.

Effects: `NavigateToSectionDetail(id: SectionId)`,
`NavigateToAttendanceDay(sectionId: SectionId, date: LocalDate)`,
`NavigateToSectionForm`, `NavigateToSchoolYears`, `NavigateToBackup`.

Note: `currentPeriodLabel` is null when today falls outside every Period
(holidays, or a year whose dates were mistyped). The banner then reads
"Fuera de periodo" and links to `Periods` rather than hiding.

Note: the card title opens `SectionDetail`. The inline button takes today's
attendance for that Section in one tap, without a detour through the hub.
Both read the same `SectionRow`; neither is destructive, so neither confirms.

---

## 4. SchoolYears

Entry: Home year switcher. Shows every School Year, marks the active one.
Primary action: FAB *Nuevo ano*.

```
+------------------------------------------+
|  <   Anos escolares                      |
+------------------------------------------+
|  +--------------------------------------+|
|  | 2026                        ACTIVO   ||
|  | 01/03 - 20/12  -  4 bimestres        ||
|  | 2 secciones                Periodos >||
|  +--------------------------------------+|
|  | 2025                                 ||
|  | 01/03 - 19/12  -  3 trimestres       ||
|  | 1 seccion                  Periodos >||
|  +--------------------------------------+|
|                                          |
|                                  (  +  ) |
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
    val dateRangeLabel: String,
    val periodKindLabel: String,
    val sectionCount: Int,
    val isActive: Boolean,
)
```

Intents: `YearClicked(id: SchoolYearId)`, `PeriodsClicked(id: SchoolYearId)`,
`AddYearClicked`, `BackClicked`.

Effects: `NavigateToPeriods(id: SchoolYearId)`, `NavigateToSetupYear`,
`NavigateBack`, `ShowMessage(text: String)`.

Note: tapping a year switches the active year; it never deletes or archives.

---

## 5. Periods

Entry: SchoolYears, or the Home "Fuera de periodo" banner.
Shows the Periods of one School Year and which contains today.
Primary action: *Guardar*.

```
+------------------------------------------+
|  <   Periodos 2026                       |
+------------------------------------------+
|  4 bimestres                             |
|                                          |
|  +--------------------------------------+|
|  | I Bimestre                           ||
|  | 01/03/2026        15/05/2026         ||
|  +--------------------------------------+|
|  | II Bimestre                 ACTUAL   ||
|  | 18/05/2026        31/07/2026         ||
|  +--------------------------------------+|
|  | III Bimestre                         ||
|  | 10/08/2026        16/10/2026         ||
|  +--------------------------------------+|
|  | IV Bimestre                          ||
|  | 19/10/2026        20/12/2026         ||
|  +--------------------------------------+|
|                                          |
|  ! Los periodos II y III se superponen.  |
+------------------------------------------+
|             [   Guardar   ]              |
+------------------------------------------+
```

```kotlin
data class PeriodsUiState(
    val isLoading: Boolean = true,
    val schoolYearLabel: String = "",
    val periodKindLabel: String = "",
    val periods: List<PeriodRow> = emptyList(),
    val overlapError: String? = null,
    val canSave: Boolean = false,
)

data class PeriodRow(
    val id: PeriodId,
    val label: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isCurrent: Boolean,
)
```

Intents: `StartDateChanged(id: PeriodId, value: LocalDate)`,
`EndDateChanged(id: PeriodId, value: LocalDate)`, `SaveClicked`, `BackClicked`.

Effects: `NavigateBack`, `ShowMessage(text: String)`.

---

## 6. SectionForm

Entry: Home FAB (create), SectionDetail overflow (rename).
Shows Grade and name. Primary action: *Guardar*.

```
+------------------------------------------+
|  <   Nueva seccion                       |
+------------------------------------------+
|  Grado                                   |
|  +----+ +----+ +----+ +----+ +----+ +--+ |
|  | 1  | | 2  | | 3  | | 4  | | 5  | |6 | |
|  +----+ +----+ +----+ +----+ +----+ +--+ |
|                                          |
|  Nombre                                  |
|  +------------------------------------+  |
|  | B                                  |  |
|  +------------------------------------+  |
|                                          |
|  ---------------------------------------  |
|  [  Eliminar seccion  ]                  |
+------------------------------------------+
|             [   Guardar   ]              |
+------------------------------------------+
```

```kotlin
data class SectionFormUiState(
    val isLoading: Boolean = true,
    val sectionId: SectionId? = null,
    val grade: Grade? = null,
    val sectionName: String = "",
    val sectionNameError: String? = null,
    val canSave: Boolean = false,
    val canDelete: Boolean = false,
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

Effects: `NavigateBack`, `ShowMessage(text: String)`.

Note: `deleteConfirmation` non-null renders the dialog naming what is lost
(US 14). An empty Section still confirms, but with zero counts.

---

## 7. SectionAreas

Entry: SectionDetail overflow, or the SetupSection link.
Shows every primary Area with a switch. Saves on toggle.

```
+------------------------------------------+
|  <   Areas de 3ro A                      |
+------------------------------------------+
|  Apaga las areas que no dictas. Nada se  |
|  borra: puedes volver a encenderlas.     |
|                                          |
|  Comunicacion                     [ ON ] |
|  Matematica                       [ ON ] |
|  Personal Social                  [ ON ] |
|  Ciencia y Tecnologia             [ ON ] |
|  Arte y Cultura                   [ ON ] |
|  Educacion Fisica                 [OFF ] |
|  Educacion Religiosa              [ ON ] |
|  Ingles                           [OFF ] |
|  Castellano como segunda lengua   [OFF ] |
|                                          |
|  ! Educacion Fisica tiene 12 niveles     |
|    registrados. Quedan guardados.        |
+------------------------------------------+
```

```kotlin
data class SectionAreasUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val areas: List<AreaToggleRow> = emptyList(),
)

data class AreaToggleRow(
    val id: AreaId,
    val name: String,
    val isActive: Boolean,
    val recordedLevelCount: Int,
)
```

Intents: `AreaToggled(id: AreaId, isActive: Boolean)`, `BackClicked`.

Effects: `NavigateBack`, `ShowMessage(text: String)`.

---

## 8. SectionDetail

Entry: Home. The hub for one Section.
Primary action: *Tomar asistencia de hoy*.

```
+------------------------------------------+
|  <   3ro A                          [.:.]|
+------------------------------------------+
|  30 alumnos  -  II Bimestre              |
|  Plantilla SIAGIE cargada                |
|                                          |
|  +--------------------------------------+|
|  |  Tomar asistencia de hoy             ||
|  |  Martes 10 de setiembre - sin tomar  ||
|  +--------------------------------------+|
|                                          |
|  +--------------------------------------+|
|  | Alumnos                       30   > ||
|  +--------------------------------------+|
|  | Asistencia                         > ||
|  +--------------------------------------+|
|  | Niveles del periodo      12 faltan > ||
|  +--------------------------------------+|
|  | Actividades                    5   > ||
|  +--------------------------------------+|
|  | Exportar                           > ||
|  +--------------------------------------+|
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
    val missingPeriodLevelCount: Int = 0,
    val activityCount: Int = 0,
)
```

`todayAttendanceSummary` reads *Sin tomar* until the first Student of the day is
recorded, then *N de M presentes*. `currentPeriodLabel`, `hasStoredTemplate`,
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
`NavigateToSectionForm(sectionId: SectionId)`, `NavigateBack`.

---

## 9. Students

Entry: SectionDetail. Lists Students by surname.
Primary action: FAB *Agregar alumno*.

```
+------------------------------------------+
|  <   Alumnos - 3ro A                [.:.]|
+------------------------------------------+
|  +------------------------------------+  |
|  | Buscar                             |  |
|  +------------------------------------+  |
|                                          |
|  | ACOSTA RIVERA, Luz Maria            > |
|  | 12345678901234                        |
|  +--------------------------------------+
|  | BAUTISTA QUISPE, Jose               > |
|  | 12345678901235                        |
|  +--------------------------------------+
|  | CCAHUANA MAMANI, Rosa               > |
|  | 12345678901236                        |
|  +--------------------------------------+
|                                          |
|  RETIRADOS (2)                        v  |
|                                          |
|                                  (  +  ) |
+------------------------------------------+
```

The top bar carries *Importar* next to *Agregar*; there is no overflow menu in
the catalog yet (`GDropdownPicker` is still planned).

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
`WithdrawnSectionToggled`, `ReactivateClicked(id: StudentId)`, `BackClicked`.

Effects: `NavigateToStudentForm(sectionId: SectionId, studentId: StudentId?)`,
`OpenDocumentPicker(mimeTypes: List<String>)`,
`NavigateToImportPreview(sectionId: SectionId, uri: String)`, `NavigateBack`,
`ShowMessage(text: String)`.

---

## 10. StudentForm

Entry: Students. Add or edit one Student. Primary action: *Guardar*.

```
+------------------------------------------+
|  <   Editar alumno                       |
+------------------------------------------+
|  Codigo del estudiante                   |
|  +------------------------------------+  |
|  | 12345678901234                     |  |
|  +------------------------------------+  |
|  14 de 14 digitos                        |
|                                          |
|  Apellidos y nombres                     |
|  +------------------------------------+  |
|  | ACOSTA RIVERA, Luz Maria           |  |
|  +------------------------------------+  |
|  Apellidos primero, como en SIAGIE.      |
|                                          |
|  ---------------------------------------  |
|  Estado                                  |
|  +----------------+  +----------------+  |
|  |    Activo      |  |   Retirado     |  |
|  +----------------+  +----------------+  |
|  Fecha de retiro                         |
|  +------------------------------------+  |
|  | 04/09/2026                         |  |
|  +------------------------------------+  |
+------------------------------------------+
|             [   Guardar   ]              |
+------------------------------------------+
```

```kotlin
data class StudentFormUiState(
    val isLoading: Boolean = true,
    val studentId: StudentId? = null,
    val studentCode: String = "",
    val studentCodeError: String? = null,
    val fullName: String = "",
    val fullNameError: String? = null,
    val isWithdrawn: Boolean = false,
    val withdrawalDate: LocalDate? = null,
    val withdrawalDateError: String? = null,
    val canSave: Boolean = false,
    val hasSiagieId: Boolean = false,
)
```

Intents: `StudentCodeChanged(value: String)`, `FullNameChanged(value: String)`,
`WithdrawnToggled(isWithdrawn: Boolean)`,
`WithdrawalDateChanged(value: LocalDate)`, `SaveClicked`, `BackClicked`.

Effects: `NavigateBack`, `ShowMessage(text: String)`.

Note: `hasSiagieId` drives a read-only hint that this Student came from a SIAGIE
Template, so editing the code by hand may break the round-trip.

---

## 11. ImportPreview

Entry: the Students top bar, after the system document picker returns a URI.
Shows what the import will do. Nothing is written until *Aplicar*.

```
+------------------------------------------+
|  <   Importar de SIAGIE                  |
+------------------------------------------+
|  3 Primaria EBR.xlsx                     |
|  3ro A  -  30 alumnos en el archivo      |
|                                          |
|  +--------------------------------------+|
|  | Se crearan                     4   v ||
|  +--------------------------------------+|
|  | Se actualizaran               26   v ||
|  +--------------------------------------+|
|  | Se propondran como retirados   2   ^ ||
|  |   [x] LOPEZ SILVA, Ana               ||
|  |   [x] TORRES PINO, Luis              ||
|  +--------------------------------------+|
|                                          |
|  Nada se borra. Los retirados conservan  |
|  su asistencia y sus niveles.            |
+------------------------------------------+
|    [ Cancelar ]      [   Aplicar    ]    |
+------------------------------------------+
```

A roster whose Student Code is missing or malformed in the middle is rejected
with the row number, never truncated at that row.

Rejection state replaces the body:

```
+------------------------------------------+
|  ! Este archivo no es de esta seccion    |
|                                          |
|  Seccion seleccionada:  3ro A            |
|  Archivo:               4to B            |
|                                          |
|  Elige otro archivo o abre la seccion    |
|  correcta.                               |
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
)

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
    val reason: String,
    val expected: String?,
    val found: String?,
)

enum class ImportGroup { CREATED, UPDATED, WITHDRAWN }
```

Intents: `GroupToggled(group: ImportGroup)`,
`WithdrawalToggled(studentId: StudentId, isSelected: Boolean)`, `ApplyClicked`,
`CancelClicked`, `BackClicked`.

Effects: `NavigateBack`, `ShowMessage(text: String)`.

---

## 12. AttendanceDay

Entry: SectionDetail primary action, or the Attendance row.
The daily workhorse. Every tap persists one row; there is no save button.

```
+------------------------------------------+
|  <   Asistencia - 3ro A            [cal] |
+------------------------------------------+
|   <    Mar 10 set 2026    >   28/30      |
|  [ Todos presentes ]        3 sin marcar |
|  Cada toque guarda al instante.          |
+------------------------------------------+
|  ACOSTA RIVERA, Luz Maria                |
|  +------+------+------+------+           |
|  |  P   |  T   |  F   |  FJ  |           |
|  +======+------+------+------+           |
|                                          |
| :BAUTISTA QUISPE, Jose : sin marcar      |
| :+------+------+------+------+:          |
| :|  P   |  T   |  F   |  FJ  |:          |
| :+------+------+------+------+:          |
|                                          |
|  CCAHUANA MAMANI, Rosa                   |
|  +------+------+------+------+           |
|  |  P   |  T   |  F   |  FJ  |           |
|  +------+------+------+======+           |
|                                          |
|  Resumen del mes  >                      |
+------------------------------------------+
```

`P` presente, `T` tardanza, `F` falta, `FJ` falta justificada. The double border
marks the selected segment; the selected segment also carries a filled
background, so the state is not colour-only. The `:`-bordered row is an
unmarked Student: a warm surface colour and a dashed outline, not colour alone,
so it stands out on a low-end screen in daylight.

```kotlin
data class AttendanceDayUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val date: LocalDate? = null,
    val dateLabel: String = "",
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
`NavigateBack`, `ShowMessage(text: String)`.

Notes:

- `isRecorded = false` means the row is showing the present default and nothing
  is stored yet (US 25, 26). The first tap creates the row. `unmarkedCount`
  counts these rows and drives the header's "N sin marcar" pill.
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

Entry: AttendanceDay footer. Monthly counts per Student, plus the SIAGIE
attendance export. Primary action: *Exportar el mes*.

```
+------------------------------------------+
|  <   Asistencia - setiembre        [cal] |
+------------------------------------------+
|   <     setiembre 2026     >             |
|                            P   T   F  FJ |
|  ACOSTA RIVERA, Luz M.    18   1   0   1 |
|  BAUTISTA QUISPE, Jose    15   2   3   0 |
|  CCAHUANA MAMANI, Rosa    20   0   0   0 |
|  DELGADO HUAMAN, Pedro    12   1   6   1 |
|                                          |
|  20 dias de clase registrados            |
+------------------------------------------+
|         [   Exportar el mes   ]          |
+------------------------------------------+
```

```kotlin
data class AttendanceMonthUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val month: YearMonth? = null,
    val monthLabel: String = "",
    val recordedDayCount: Int = 0,
    val rows: List<AttendanceMonthRow> = emptyList(),
    val canExport: Boolean = false,
    val exportUnavailableReason: String? = null,
    val isExporting: Boolean = false,
)

data class AttendanceMonthRow(
    val studentId: StudentId,
    val displayName: String,
    val presentCount: Int,
    val lateCount: Int,
    val absentCount: Int,
    val justifiedCount: Int,
)
```

Intents: `PreviousMonthClicked`, `NextMonthClicked`,
`MonthPicked(value: YearMonth)`, `ExportClicked`, `BackClicked`.

Effects: `ShareFile(path: String, mimeType: String)`, `NavigateBack`,
`ShowMessage(text: String)`.

---

## 14. WorkedCompetencies

Entry: PeriodLevels empty state or its top bar. One Area, one Period.
Saves on toggle.

```
+------------------------------------------+
|  <   Competencias trabajadas             |
|      Personal Social - II Bimestre       |
+------------------------------------------+
|  Marca solo las competencias que         |
|  trabajaste. Solo esas se exportan.      |
|                                          |
|  [x] 01  Construye su identidad          |
|  [x] 02  Convive y participa democrat... |
|  [ ] 03  Construye interpretaciones...   |
|  [ ] 04  Gestiona responsablemente el... |
|  [x] 05  Gestiona responsablemente los   |
|          recursos economicos             |
|                                          |
|  3 de 5 marcadas                         |
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

Effects: `NavigateBack`, `ShowMessage(text: String)`.

Note: unmarking a Competency that already has Period Levels warns but never
deletes; the levels stop being exported and reappear if it is remarked.

---

## 15. PeriodLevels

Entry: SectionDetail. The grid for one Section x Period x Area.
Primary action: tap a cell.

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
`GLevelChip(isCurrent = true)`. A single
tap on a level closes that Student's row and reopens the picker for the next
one; *Listo* or picking a level for the last Student exits column mode. A tap
on any other cell while column mode is closed still opens `PeriodLevelSheet`
for that one Student x Competency, Descriptive Conclusion included.

```kotlin
data class PeriodLevelsUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val areas: List<AreaOption> = emptyList(),
    val selectedAreaId: AreaId? = null,
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

data class AreaOption(val id: AreaId, val name: String)

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

Intents: `AreaSelected(id: AreaId)`, `PeriodSelected(id: PeriodId)`,
`CellClicked(key: PeriodLevelCellKey)`, `MissingFilterToggled`,
`EnterColumnMode(competencyId: CompetencyId)`,
`PickLevelForCurrent(level: AchievementLevel?)`, `ExitColumnMode`,
`WorkedCompetenciesClicked`, `BackClicked`.

Effects: `NavigateToWorkedCompetencies(sectionId: SectionId, areaId: AreaId, periodId: PeriodId)`,
`NavigateBack`, `ShowMessage(text: String)`.

---

## 16. PeriodLevelSheet

A bottom sheet over PeriodLevels for one Student x one Competency.
Every change persists on selection; the sheet closes with *Listo*.

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
    val isLoading: Boolean = true,
    val studentName: String = "",
    val competencyLabel: String = "",
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
    val level: AchievementLevel,
)
```

Intents: `AchievementLevelSelected(level: AchievementLevel?)`,
`UnworkedCommentSelected(comment: UnworkedComment?)`,
`DescriptiveConclusionChanged(value: String)`, `DoneClicked`, `DismissRequested`.

Effects: `Dismiss`, `ShowMessage(text: String)`.

Notes:

- Selecting an Achievement Level clears the Unworked Comment and vice versa: a
  Period Level holds exactly one of the two (ADR 0003). The state carries both
  fields because the sheet must render the transition, but the write enforces
  exclusivity in the domain.
- `--- ` in the level row is the explicit "sin nivel" option; clearing a level is
  an action, not a long-press or a swipe.
- `evidence` is read-only. There is no control on this sheet that copies an
  Evidence Level into the Period Level.
- `evidence` does not exist yet. Evidence Levels arrive with Activities, so
  ticket #11 adds the field and the section that renders it; until then the
  sheet has neither, rather than an empty placeholder.

---

## 17. Activities

Entry: SectionDetail. Activities of the current Period, newest first.
Primary action: FAB *Nueva actividad*.

```
+------------------------------------------+
|  <   Actividades - 3ro A                 |
+------------------------------------------+
|  II Bimestre v                           |
|                                          |
|  | 05/07  Ficha de convivencia         > |
|  | PPSS 02  -  24 de 30 con evidencia    |
|  +--------------------------------------+
|  | 22/06  Debate del aula              > |
|  | PPSS 01, PPSS 02  -  30 de 30         |
|  +--------------------------------------+
|  | 10/06  Lectura en grupo             > |
|  | COMU 01  -  18 de 30                  |
|  +--------------------------------------+
|                                          |
|                                  (  +  ) |
+------------------------------------------+
```

```kotlin
data class ActivitiesUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val periods: List<PeriodOption> = emptyList(),
    val selectedPeriodId: PeriodId? = null,
    val activities: List<ActivityRow> = emptyList(),
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

---

## 18. ActivityForm

Entry: Activities. Primary action: *Guardar*.

```
+------------------------------------------+
|  <   Nueva actividad                     |
+------------------------------------------+
|  Nombre                                  |
|  +------------------------------------+  |
|  | Debate del aula                    |  |
|  +------------------------------------+  |
|                                          |
|  Fecha                                   |
|  +------------------------------------+  |
|  | 22/06/2026                         |  |
|  +------------------------------------+  |
|  Cae en el II Bimestre.                  |
|                                          |
|  Competencias trabajadas                 |
|  PERSONAL SOCIAL                         |
|  [x] 01  Construye su identidad          |
|  [x] 02  Convive y participa democrat... |
|  COMUNICACION                            |
|  [ ] 01  Se comunica oralmente           |
|                                          |
|  ---------------------------------------  |
|  [  Eliminar actividad  ]                |
+------------------------------------------+
|             [   Guardar   ]              |
+------------------------------------------+
```

```kotlin
data class ActivityFormUiState(
    val isLoading: Boolean = true,
    val activityId: ActivityId? = null,
    val name: String = "",
    val nameError: String? = null,
    val date: LocalDate? = null,
    val resolvedPeriodLabel: String? = null,
    val periodChangeWarning: String? = null,
    val competencyGroups: List<CompetencyGroup> = emptyList(),
    val selectedCompetencyIds: Set<CompetencyId> = emptySet(),
    val canSave: Boolean = false,
    val canDelete: Boolean = false,
    val isDeleteConfirmVisible: Boolean = false,
)

data class CompetencyGroup(
    val areaId: AreaId,
    val areaName: String,
    val competencies: List<CompetencyToggleRow>,
)
```

Intents: `NameChanged(value: String)`, `DateChanged(value: LocalDate)`,
`CompetencyToggled(id: CompetencyId, isSelected: Boolean)`, `SaveClicked`,
`DeleteClicked`, `DeleteConfirmed`, `DeleteDismissed`, `BackClicked`.

Effects: `NavigateToActivityEvidence(activityId: ActivityId)`, `NavigateBack`,
`ShowMessage(text: String)`.

Notes:

- `resolvedPeriodLabel` comes from the date (US 35); the Period is never picked.
- `periodChangeWarning` is non-null while editing when the new date moves the
  Activity to a different Period.
- The competency list is restricted to Worked Competencies of the resolved
  Period and active Areas.

---

## 19. ActivityEvidence

Entry: Activities. One Evidence Level per Student per Competency of this
Activity. Saves on tap.

```
+------------------------------------------+
|  <   Debate del aula                [.:.]|
|      22/06/2026 - II Bimestre            |
+------------------------------------------+
|  PPSS 01 v                    24/30      |
+------------------------------------------+
|  ACOSTA RIVERA, Luz Maria                |
|  +----+ +----+ +----+ +----+ +-------+   |
|  | AD | | A  | | B  | | C  | |  ---  |   |
|  +====+ +----+ +----+ +----+ +-------+   |
|                                          |
|  BAUTISTA QUISPE, Jose                   |
|  +----+ +----+ +----+ +----+ +-------+   |
|  | AD | | A  | | B  | | C  | |  ---  |   |
|  +----+ +----+ +====+ +----+ +-------+   |
|                                          |
|  CCAHUANA MAMANI, Rosa                   |
|  +----+ +----+ +----+ +----+ +-------+   |
|  | AD | | A  | | B  | | C  | |  ---  |   |
|  +----+ +----+ +----+ +----+ +=======+   |
+------------------------------------------+
```

With several Competencies the screen keeps a competency selector at the top
rather than widening the row: one Competency at a time, one tap per Student.

```kotlin
data class ActivityEvidenceUiState(
    val isLoading: Boolean = true,
    val activityName: String = "",
    val activityDateLabel: String = "",
    val periodLabel: String = "",
    val competencies: List<CompetencyColumn> = emptyList(),
    val selectedCompetencyId: CompetencyId? = null,
    val recordedCount: Int = 0,
    val totalCount: Int = 0,
    val rows: List<EvidenceLevelRow> = emptyList(),
)

data class EvidenceLevelRow(
    val studentId: StudentId,
    val displayName: String,
    val level: AchievementLevel?,
)
```

Intents: `CompetencySelected(id: CompetencyId)`,
`LevelSelected(studentId: StudentId, level: AchievementLevel?)`,
`EditActivityClicked`, `BackClicked`.

Effects: `NavigateToActivityForm(sectionId: SectionId, activityId: ActivityId?)`,
`NavigateBack`, `ShowMessage(text: String)`.

---

## 20. Export

Entry: SectionDetail. All three outputs for one Section and Period.

```
+------------------------------------------+
|  <   Entregar - 3ro A                    |
+------------------------------------------+
|  II Bimestre v                           |
|                                          |
|  +--------------------------------------+|
|  | Notas para SIAGIE                    ||
|  | 3 Primaria EBR.xlsx                  ||
|  | ! 2 conclusiones descriptivas faltan ||
|  |   BAUTISTA QUISPE, Jose            > ||
|  |   Personal Social - 02                ||
|  |   DELGADO HUAMAN, Pedro            > ||
|  |   Comunicacion - 01                   ||
|  |          [  Generar archivo  ]       ||
|  +--------------------------------------+|
|                                          |
|  +--------------------------------------+|
|  | Asistencia para SIAGIE               ||
|  | setiembre 2026                       ||
|  |             [     Exportar       ]   ||
|  +--------------------------------------+|
|                                          |
|  +--------------------------------------+|
|  | Resumen para imprimir                ||
|  |    [   PDF   ]      [   CSV    ]     ||
|  +--------------------------------------+|
|                                          |
|  El archivo conserva su nombre original. |
+------------------------------------------+
```

When there are no blockers, the grades card drops the gap list and its
*Generar archivo* button enables:

```
|  | Notas para SIAGIE                    ||
|  | 3 Primaria EBR.xlsx                  ||
|  |          [  Generar archivo  ]       ||
```

When no Template is stored, the first card reads:

```
|  | Notas para SIAGIE          NO DISPO. ||
|  | Esta seccion no tiene una plantilla  ||
|  | SIAGIE importada. Importa una desde  ||
|  | Alumnos, o usa el resumen PDF/CSV.   ||
|  |             [  Importar plantilla ]  ||
```

```kotlin
data class ExportUiState(
    val isLoading: Boolean = true,
    val sectionTitle: String = "",
    val periods: List<PeriodOption> = emptyList(),
    val selectedPeriodId: PeriodId? = null,
    val templateFileName: String? = null,
    val gradesExportState: GradesExportState = GradesExportState.Unavailable,
    val attendanceMonthLabel: String = "",
    val isExporting: Boolean = false,
)

sealed interface GradesExportState {
    data object Unavailable : GradesExportState
    data object Ready : GradesExportState
    data class Blocked(val gaps: List<ExportGapRow>) : GradesExportState
}

data class ExportGapRow(
    val studentId: StudentId,
    val studentName: String,
    val areaName: String,
    val siagieOrdinal: Int,
    val competencyId: CompetencyId,
)
```

Intents: `PeriodSelected(id: PeriodId)`, `ExportGradesClicked`,
`GapRowClicked(row: ExportGapRow)`, `ExportAttendanceClicked`, `ExportPdfClicked`,
`ExportCsvClicked`, `ImportTemplateClicked`, `BackClicked`.

Effects: `ShareFile(path: String, mimeType: String)`,
`NavigateToPeriodLevelCell(studentId: StudentId, competencyId: CompetencyId)`,
`NavigateToStudents(sectionId: SectionId)`, `NavigateBack`,
`ShowMessage(text: String)`.

Note: screen title is "Entregar", because that is the Teacher's goal, not the
file format. Every blocking gap now lists as a tap-through row inside the
grades card itself — no separate bottom sheet — and `ExportGradesClicked`
(now labelled *Generar archivo*) is enabled only when `gradesExportState` is
`Ready`, i.e. nothing is pending.

---

## 21. Backup

Entry: Home overflow or the reminder banner.

```
+------------------------------------------+
|  <   Respaldo                            |
+------------------------------------------+
|  Ultimo respaldo                         |
|  hace 12 dias  -  29/08/2026             |
|                                          |
|  +--------------------------------------+|
|  |       Crear respaldo                 ||
|  +--------------------------------------+|
|  Se crea un archivo .gema y se abre el   |
|  menu para compartir.                    |
|                                          |
|  ---------------------------------------  |
|  Restaurar                               |
|  +--------------------------------------+|
|  |    Elegir archivo .gema              ||
|  +--------------------------------------+|
|  ! Restaurar reemplaza TODO lo que hay   |
|    en este telefono y reinicia la app.   |
|                                          |
|  ---------------------------------------  |
|  Recordarme cada                         |
|  +----------+                            |
|  |    7     | dias                       |
|  +----------+                            |
+------------------------------------------+
```

```kotlin
data class BackupUiState(
    val isLoading: Boolean = true,
    val lastBackupDate: LocalDate? = null,
    val lastBackupLabel: String = "",
    val reminderThresholdDays: Int = 7,
    val reminderThresholdError: String? = null,
    val isCreating: Boolean = false,
    val restoreConfirmation: RestoreConfirmation? = null,
    val isRestoring: Boolean = false,
)

data class RestoreConfirmation(
    val fileName: String,
    val currentSchoolYearCount: Int,
    val currentStudentCount: Int,
)
```

Intents: `CreateBackupClicked`, `ChooseRestoreFileClicked`,
`RestoreFilePicked(uri: String)`, `RestoreConfirmed`, `RestoreDismissed`,
`ReminderThresholdChanged(value: String)`, `BackClicked`.

Effects: `ShareFile(path: String, mimeType: String)`,
`OpenDocumentPicker(mimeTypes: List<String>)`, `RestartApp`, `NavigateBack`,
`ShowMessage(text: String)`.

Note: `isCreating` and `isRestoring` are the only justified progress indicators
in the app; copying a database file is real local work with a visible duration,
unlike a read.

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
