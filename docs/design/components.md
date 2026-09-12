# `core/ui` component catalog

Every component a feature screen in `screens.md` needs, and nothing else. The
iron rule from `.claude/rules/ui-components.md` holds: a feature screen calls
`G*` components only, never raw Material3, and never a literal colour, size or
radius.

Built path: `core/ui/src/main/kotlin/com/emm/gema/core/ui/` — the foundation
split has landed, `core:ui` is its own module. Tokens live in
`core/theme/Color.kt`, `Type.kt` and `Foundation.kt` and are the single source
of truth. Values below follow the Registro tokens in `system.md`; that
document is authoritative for anything not restated here.

## Token vocabulary used below

| Token group | Members referenced here |
|---|---|
| `GemaSpacing` | `extraSmall`, `small`, `medium`, `large`, `extraLarge`, `screenGutter`, `minimumTouchTarget` |
| `GemaShapes` | `control` 8dp (buttons, inputs, chips), `chip` 6dp (evidence chips), `container` 12dp (cards, sheets), `pill` |
| `MaterialTheme.colorScheme` | `surface`, `surfaceContainerLow`, `surfaceContainerHigh`, `onSurface`, `onSurfaceVariant`, `outlineVariant`, `outline`, `primary`, `primaryContainer`, `onPrimaryContainer`, `error`, `errorContainer`, `inverseSurface` |
| `GemaAccents` | `warningContainer` / `onWarningContainer` (light `#FBF4E1`/`#5C4A12`, dark `#3A3222`/`#EAD79C`) — values live in `Color.kt`. #163: `unmarkedSurface`/`onUnmarkedSurface` and `absentContainer`/`onAbsentContainer` are deleted, not renamed; `GAttendanceRow` moves to this warning pair (unmarked) and `colorScheme.errorContainer` (absent) |
| `gemaTypography` | `headlineSmall`, `titleLarge`, `titleMedium`, `bodyLarge`, `bodySmall`, `labelLarge`, `labelSmall` — `GTextStyle.NUMERAL` is resolved directly in `GText.kt`, not a `Typography` slot |

`minimumTouchTarget` is 48dp and is a hard floor for every interactive
component in this catalog. It is a token and not a per-component constant
precisely because every component must honour it.

## The set

Forty-seven `G*`-prefixed Kotlin files exist under
`core/ui/src/main/kotlin/com/emm/gema/core/ui/` today (counted directly:
`fd -e kt . core/ui/src/main | rg -c '/G[A-Z]'`). That count includes
component files and their co-located enums (`GButtonVariant`, `GBannerTone`,
`GLevelOption`, and similar), not forty-seven independently useful widgets —
the table below lists the components a feature screen actually calls. Each is
justified by at least two screens; anything used once lives in its feature
package instead (`.claude/rules/ui-components.md`, "Decide the scope").

| Component | Wraps | Used by | Status |
|---|---|---|---|
| `GScreen` | `Scaffold` | every screen | built |
| `GTopBar` | `TopAppBar` | every screen | built |
| `GText` | `Text` | every screen | built |
| `GButton` | `Button` / `OutlinedButton` / `TextButton` | setup, forms, export, backup | built |
| `GIconButton` | `IconButton` | top bars, date stepper | built |
| `GTextField` | `OutlinedTextField` | setup, student form, activity form, conclusion, backup | built |
| `GDateField` | `OutlinedTextField` + `DatePickerDialog` | setup, periods, student form, activity form | built |
| `GSegmentedPicker` | `SingleChoiceSegmentedButtonRow` | period kind, attendance status, level pickers | built |
| `GChoiceChipRow` | `Surface` row | grade picker | built |
| `GStepHeader` | `Column` + `GText` | setup year, setup section | built |
| `GListItem` | `ListItem` inside `Surface` | home, sections, students, activities, export gaps | built |
| `GCard` | `Surface` | home banner, export cards, section detail | built |
| `GLevelChip` | `Surface` + `Text` | period levels grid, evidence rows | built |
| `GLevelPicker` | `GSegmentedPicker` | period level sheet, activity evidence | built |
| `GAttendanceToggle` | `GSegmentedPicker` | attendance day | built |
| `GCheckRow` | `Row` + `Checkbox` | worked competencies, activity form, import preview | built |
| `GRadioRow` | `Row` + `RadioButton` | period level sheet | built |
| `GSwitchRow` | `Row` + `Switch` | section areas | built |
| `GBanner` | `Surface` | backup reminder, import rejection, export blocked, period warnings | built |
| `GEmptyState` | `Column` | students, activities, period levels, sections | built |
| `GDialog` | `AlertDialog` | delete section, restore backup, apply import | built |
| `GBottomSheet` | `ModalBottomSheet` | period level sheet | built |
| `GDropdownPicker` | `ExposedDropdownMenuBox` | area, period and month selectors, export period | built |
| `GSearchField` | `BasicTextField` | students | built |
| `GExtendedFab` | `ExtendedFloatingActionButton` | students, sections, school years, activities | built |
| `GGroupHeader` | `Surface` + `Text` | students | built |
| `GYearCard` | `Surface` + `Text` | school years | built |
| `GBadge` | `Surface` + `Text` | school years | built |
| `GTableHeaderBand` | `Surface` + `HorizontalDivider` | attendance month | built |
| `GTableRow` | `Box` + `HorizontalDivider` | attendance month | built |
| `GMonthPickerDialog` | `AlertDialog` (`GDialog`) + `Surface` month chips | attendance month | built |

(Thirty-one rows above, all built; `GScreen`, `GDialog` and `GBottomSheet`
are structural shells rather than widgets, which is why the working widget
set is twenty-eight. `GTableHeaderBand`/`GTableRow` currently back one
screen; the period levels grid drifts on the same shape and is its own
migration ticket. The remaining `G*` files under `core/ui/src/main` beyond
this table — `GAttendanceRow`, `GBorderedContainer`, `GCalendarIconButton`,
`GCircledIcon`, `GCompactNote`, `GDateChip`, `GDivider`, `GFileCard`, `GIcon`,
`GOverflowMenu`, `GTintedGroupContent`, `GValidatedHelperText`, plus the
co-located enums `GButtonVariant`, `GBannerActionStyle`, `GBannerTone` and
`GLevelOption` — are real and shipped but not repeated in this table because
they are either an enum or a narrow supporting piece of a component listed
above.)

---

### GText

Purpose: every piece of text in a feature screen goes through this, so a
typography value never gets hardcoded outside `com.emm.gema.core.theme.Type`.

```kotlin
enum class GTextStyle {
    TITLE_MEDIUM,
    TITLE_MEDIUM_EMPHASIS,
    TITLE_LARGE_EMPHASIS,
    BODY_LARGE,
    BODY_SMALL,
    LABEL_LARGE_EMPHASIS,
    LABEL_SMALL,
    LABEL_SMALL_EMPHASIS,
    NUMERAL,
}

@Composable
fun GText(
    text: String,
    modifier: Modifier = Modifier,
    style: GTextStyle = GTextStyle.BODY_LARGE,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
)
```

Wraps `Text`. `GTextStyle` is a closed set mapped 1:1 to the `MaterialTheme.typography`
members the app actually uses — not the full fifteen Material type-scale slots,
and not a semantic name like "Title" or "Caption" that would drift from the
token it maps to. One exception: `NUMERAL` is not a `Typography` slot —
`GText.kt` resolves it directly as `titleLarge.copy(fontWeight =
FontWeight.SemiBold, fontFeatureSettings = "tnum")` (20sp / weight 600 /
lineHeight 24sp, tabular figures, color `onSurface`), replacing
`CARD_TITLE_EMPHASIS`, `gemaCardTitleFontSize` and `gemaCardDateFontSize`
(#163 scope). `#163` also drops `BODY_MEDIUM`, `TITLE_SMALL` and
`LABEL_MEDIUM` from this enum — see `system.md`'s Typography section for the
remap. `color` defaults to `Color.Unspecified`, so `Text` falls back to
`LocalContentColor` exactly as a raw `Text` call would.

### GScreen

Purpose: the one scaffold every screen uses, so the gutter, the insets and the
bottom action bar are decided once instead of in twenty places.

```kotlin
@Composable
fun GScreen(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    contentGutter: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
)
```

Wraps `Scaffold`. Tokens: `GemaSpacing.screenGutter` applied as horizontal
padding to both `content` and `bottomAction`, `colorScheme.surface` as the
background.

Contract: `GScreen` owns the horizontal gutter once. A screen never adds
horizontal `screenGutter` to its root column.

Contract (#68): `fab` and `bottomAction` are mutually exclusive. `GScreen`
renders only one bottom slot, so passing both raises
`IllegalArgumentException` instead of silently stacking a FAB over a bottom
action bar. A screen picks one.

`contentGutter` (default `true`) turns that padding off for `content` when a
screen's body is full-bleed by design — a table with an edge-to-edge header
band and dividers, for instance — and the inner cells carry their own
horizontal inset instead (attendance month). `bottomAction` keeps its gutter
regardless of `contentGutter`; the primary button is never full-bleed.

Deviation from an earlier draft of this catalog: the built signature carries
`snackbarHostState: SnackbarHostState?`, not a `floatingAction` slot. There is
no floating action button anywhere in this app (see "No `GTabRow`" and the
rest of the "deliberately not in the catalog" list below for the same
minimalism); the one host a screen needs is the `SnackbarHost` for pure
acknowledgements such as "Respaldo creado", wired once here instead of once
per screen.

Tradeoff: `bottomAction` exists instead of leaving primary buttons inline in the
scroll because on a 4.5" screen the primary action is otherwise below the fold,
and a Teacher holding the phone one-handed cannot reach a top-right "Guardar".
It costs vertical space, which is the scarcest resource on these devices — so
only screens that genuinely commit something use it.

### GTopBar

Purpose: title, back affordance and at most two actions.

```kotlin
@Composable
fun GTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
)
```

Wraps `TopAppBar`. Tokens: `gemaTypography.titleMedium` for `title`,
`gemaTypography.labelSmall` + `onSurfaceVariant` for `subtitle`, `colorScheme.surface`.

Tradeoff: no collapsing or large top bar. A large top bar animates on every
scroll, which costs frames on a low-end device and eats a fifth of the viewport
on a screen whose job is showing thirty rows.

### GButton

Purpose: every committing action.

```kotlin
enum class GButtonVariant { PRIMARY, SECONDARY, DESTRUCTIVE, TEXT }

@Composable
fun GButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: GButtonVariant = GButtonVariant.PRIMARY,
    enabled: Boolean = true,
    isBusy: Boolean = false,
    icon: ImageVector? = null,
)
```

Wraps `Button` (primary), `OutlinedButton` (secondary), `Button` with
`colorScheme.error` (destructive), `TextButton` (text). Tokens:
`GemaShapes.control`, `GemaSpacing.medium` horizontal padding,
`GemaSpacing.minimumTouchTarget` height floor, `gemaTypography.labelLarge`,
`GemaSpacing.small` between `icon` and label when `icon` is set.

Deviation from an earlier draft of this catalog: the built parameter is
`enabled`, matching Compose's own `Button`/`OutlinedButton`/`TextButton`
convention, not `isEnabled` (the `is`-prefix convention `naming.md` uses for
booleans elsewhere in this codebase — `GTextField`, `GDateField`,
`GSegmentedPicker` and the rest of this catalog all use `isEnabled`, so
`GButton` is the one outlier). `icon` was added for the attendance month
export action (docs/design/mockups/attendance-month.html); `isBusy` hides it,
matching the plain-text busy state.

Tradeoff: `isBusy` exists but is used on exactly three actions — create Backup,
restore Backup and produce an export file. Everything else in this app writes to
a local database in single-digit milliseconds; wiring a busy state to those
would create a flicker that reads as a bug. A variant enum rather than four
composables keeps the call sites uniform and the exhaustive `when` in one file.

### GIconButton

```kotlin
@Composable
fun GIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
)
```

Wraps `IconButton`. Tokens: `GemaSpacing.minimumTouchTarget`,
`colorScheme.onSurfaceVariant`, `onSurface` when enabled and prominent,
`colorScheme.outline` when `isEnabled` is false.

`contentDescription` is required, not nullable: an icon-only control with no
label is the one place where a missing description makes the app unusable with
TalkBack, and several Teachers work with the screen at arm's length in bad
light.

### GTextField

```kotlin
@Composable
fun GTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isEnabled: Boolean = true,
)
```

Wraps `OutlinedTextField`, built on the existing `FieldShell` template
(animated border, transparent background, 48dp floor) named in
`.claude/rules/ui-components.md`.

Tokens: `GemaShapes.control`, `colorScheme.outline` / `primary` for the border,
`colorScheme.error` for `errorText`, `gemaTypography.bodyLarge` for the value and
`gemaTypography.labelSmall` for the supporting line.

Deviation from an earlier draft of this catalog: the built component has no
`placeholder` and no `maxLines` — every use so far is a single labelled line,
so both would be unused parameters. It also does **not** show `errorText` and
`supportingText` at once: the supporting line renders `errorText ?: supportingText`,
so an error replaces the supporting text rather than sitting beside it. The
Student Code live digit counter and its uniqueness error therefore cannot both
be visible through this component as built; a screen that genuinely needs both
at once has to compose its own supporting row until that need is confirmed
against a real screen.

### GDateField

Purpose: one date, entered the same way everywhere.

```kotlin
@Composable
fun GDateField(
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    isEnabled: Boolean = true,
)
```

Wraps a read-only `GTextField` that opens `DatePickerDialog`. Tokens: inherited
from `GTextField`.

Tradeoff: the picker opens in **calendar** mode, not the text-input mode
Material3 defaults to for some locales, and typing into the field is disabled.
Free text invites `03/09` vs `09/03` ambiguity, and a Period boundary typed
wrong silently misfiles months of Activities. `minDate` / `maxDate` exist so
AttendanceDay can make future dates unreachable rather than rejecting them after
the tap.

Line behavior: the field is not `singleLine`. It is capped at two lines with a
one-line minimum, so the `DD/MM/YYYY` value stays on one line wherever it fits
and reflows onto a second line only where the container is too narrow for it —
`AlertDialog` is content-width, so `GDialog` squeezes a `weight(1f)` pair well
below what the same pair gets on a full screen. Reflow is driven by the text's
own layout against the real container, so it holds at every font scale; a
`singleLine` field would clip the value instead, and a font-scale threshold
cannot see the container at all.

### GSegmentedPicker

Purpose: the one horizontal exclusive-choice control. `GLevelPicker` and
`GAttendanceToggle` are both built on it.

```kotlin
@Composable
fun <T> GSegmentedPicker(
    options: List<GSegmentOption<T>>,
    selected: T?,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
)

data class GSegmentOption<T>(
    val value: T,
    val label: String,
    val contentDescription: String,
)
```

Wraps `SingleChoiceSegmentedButtonRow`. Tokens: `GemaShapes.control` on the
outer row, `colorScheme.primary` / `onPrimary` for the selected segment,
`colorScheme.surface` (no fill) / `onSurface` text for the unselected state
with an `outline` divider between segments, `GemaSpacing.minimumTouchTarget`
as the segment height floor, `gemaTypography.labelLarge`.

`GLevelPicker` and `GAttendanceToggle` override this default selected-state
styling with their own rules — see system.md: `GLevelPicker` uses a
`primaryContainer` fill while the letter itself stays `onSurface` ink rather
than switching to `onPrimary`; `GAttendanceToggle` uses a different fill per
attendance status instead of one shared `primary` selected color.

Tradeoffs worth stating, because this is the control the app lives on:

- **Segmented control, not a dropdown.** Attendance and levels are set hundreds
  of times a day. A dropdown is two taps and an animation; a segment is one tap.
- **`selected: T?` and `onSelect: (T?)`.** Nullable both ways, because "no
  level" (US 37) and "not yet recorded" are real, meaningful states in this
  domain, not the absence of data. Re-tapping the selected segment clears it, so
  clearing never needs a long-press — long-press is undiscoverable and hard with
  a gloved finger.
- **Maximum five segments.** At 360dp with a 48dp floor, six segments fall below
  a comfortable target. `GLevelPicker` sits exactly at five (AD, A, B, C, none)
  and `GAttendanceToggle` at four. Anything wider must become a list, not a
  narrower segment.
- **`contentDescription` per option.** "AD" and "FJ" are meaningless to a screen
  reader; the description carries the full wording.

### GListItem

```kotlin
@Composable
fun GListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingText: String? = null,
    trailingText: String? = null,
    hasChevron: Boolean = false,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
)
```

Wraps `ListItem` inside a clickable `Surface`. Tokens: `GTextStyle.BODY_LARGE`
(the `titleStyle` default) for `title`, `gemaTypography.bodyLarge` for
`subtitle` and `leadingText`, `gemaTypography.bodySmall` for `trailingText`,
`colorScheme.onSurface` / `onSurfaceVariant`, `colorScheme.primary` for
`leadingText`, `GemaSpacing.leadingLabelWidth` for its fixed width,
`GemaSpacing.medium` vertical padding, `colorScheme.outline` for the
hairline divider.

Tradeoff: no leading avatar or icon slot — `leadingText` is a short label
(a Roman numeral, an ordinal), not an image slot. Every list in this app is
people or dated records; an icon column would cost 40dp of a 360dp width and
carry no information. Names are long — "CCAHUANA MAMANI, Rosa Elena" — and the
width is better spent on them.

`leadingText` and `showDivider` were added for SetupYear's grouped Periodos
list (#53): several `GListItem`s stacked inside one `GCard` read as a single
bordered list only when the last row skips its divider, and the Roman numeral
needed a fixed-width slot next to the range instead of a stacked subtitle.

### GCard

```kotlin
@Composable
fun GCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
)
```

Wraps `Surface` (not `Card`). Tokens: `GemaShapes.container` (12dp radius),
`colorScheme.surface` with a hairline `outlineVariant` border, `GemaSpacing.medium`
inner padding.

Tradeoff: `Surface` with a one-dp outline instead of `Card` with elevation.
Elevation shadows are the single most expensive thing to draw repeatedly on a
low-end GPU, and in direct sunlight a shadow is invisible anyway — an outline
is not.

Deviation from an earlier draft of this catalog: `GCard`'s `containerColor`
defaults to `colorScheme.surface`, not `surfaceVariant`. Registro cards lose
their tint entirely — a card is white with a hairline border, never a color.
Content that is genuinely one line uses a `GListItem` row instead of a card.

Deviation from an earlier draft of this catalog: the built `GCard` has no
`onClick`. Every current use (home banner, export cards, section detail) is a
passive container; a clickable variant of the whole card was never needed, and
a caller that wants a tap target wraps its own `Modifier.clickable` on the
content rather than the catalog adding a parameter with no current caller.

### GBorderedContainer

```kotlin
@Composable
fun GBorderedContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
)
```

Wraps `Surface`. A non-card grouping surface for content that is not a
floating card — the Period Levels grid, a bottom sheet body. Tokens: see
`system.md`, "GBorderedContainer" (Component rules).

### GLevelChip

Purpose: display one Achievement Level, one Unworked Comment, or an empty slot.
Read-only.

```kotlin
enum class GLevelChipSize { GRID, INLINE, EVIDENCE }

enum class GLevelOption(val letter: String, val contentDescription: String) {
    AD("AD", "Logro destacado"),
    A("A", "Logro esperado"),
    B("B", "En proceso"),
    C("C", "En inicio"),
}

@Composable
fun GLevelChip(
    letter: String?,
    modifier: Modifier = Modifier,
    hasUnworkedComment: Boolean = false,
    isIncomplete: Boolean = false,
    isCurrent: Boolean = false,
    size: GLevelChipSize = GLevelChipSize.INLINE,
    onClick: (() -> Unit)? = null,
)
```

Wraps `Surface` + `Text`. Sizes: GRID 48×44dp inside a 56dp row (`labelSmall`),
INLINE 48×48dp (`labelLarge`), EVIDENCE 34×26dp with `GemaShapes.chip` radius
(`labelSmall`) — GRID and INLINE both use `GemaShapes.control`. Tokens:
`colorScheme.surface` background for both EVIDENCE and GRID/INLINE (no
fill), `colorScheme.outline` border for GRID/INLINE (`outlineVariant` for an
EVIDENCE default),
`colorScheme.primary` for the `isCurrent` border, `colorScheme.error` for the
`isIncomplete` border and marker, `colorScheme.onSurface` for the letter in
every state.

Tradeoffs, and this is the one place where the obvious choice is wrong:

- **The four levels are not four colours.** A traffic-light palette — AD green,
  C red — would be readable at a glance, and `.claude/rules/ui-components.md`
  forbids it explicitly: semantic colours mean system state, never a grade.
  There is also a pedagogical reason the rule is right. The CNEB literal scale
  is a description of where a child is, not a pass/fail, and a grid of red cells
  shown to a parent or a director reframes it as one. The chip therefore carries
  the **letter**, in one neutral surface, at a size that is legible without
  colour. Distinguishing levels is the Teacher's reading job, which they do
  fluently; the app's job is not to editorialise. **Invariant: a level letter is
  always `onSurface`; only `isIncomplete` uses `error`.**
- **`isCurrent` is a border, not a fill.** Column mode (ADR 0015) has to say
  which cell the bottom picker is bound to. It thickens the border to
  `GemaSpacing.indicatorStroke` in `colorScheme.primary`, so the letter itself
  is untouched and the marker survives next to `isIncomplete`, which wins the
  border colour when both are true.
- **`isIncomplete` is the only coloured state**, using `colorScheme.error`,
  because that *is* a system state: SIAGIE will reject the file. It renders as a
  marker glyph next to the letter, never as a fill, so it survives a colour-blind
  reader and a washed-out screen.
- **Three sizes, not a free `dp`.** GRID has to fit three columns plus a pinned
  name column at 360dp; EVIDENCE has to fit inline in a read-only evidence row
  on `PeriodLevelSheet`. A caller passing an arbitrary size would eventually
  break that arithmetic.
- **`letter: String?`, not `GLevelOption` directly.** `core:ui` is the design
  system and depends on no domain module, the same boundary `GAttendanceOption`
  keeps for `GAttendanceToggle`. The chip takes a plain letter; `GLevelOption`
  supplies the letter and `contentDescription` pairing used by `GLevelPicker`,
  and `feature:evaluation` maps `AchievementLevel` to it. The cost is one
  exhaustive `when` in the feature module; the gain is a design system that a
  second app, or a redesign, can take without the CNEB scale coming along.

### GLevelPicker

```kotlin
@Composable
fun GLevelPicker(
    selected: GLevelOption?,
    onSelect: (GLevelOption?) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
)
```

Wraps `GSegmentedPicker` with the five fixed options AD / A / B / C / none.
Tokens: see `system.md`, "GLevelPicker" (Component rules).

It exists as its own composable rather than a call-site configuration of
`GSegmentedPicker` because the option list, their order and their
`contentDescription`s must be identical on the Period Level sheet, on the column
mode bar and on Activity Evidence. Three screens building the same list
independently is how they drift.

### GAttendanceToggle

```kotlin
enum class GAttendanceOption(val label: String, val contentDescription: String) {
    PRESENT("P", "Presente"),
    LATE("T", "Tardanza"),
    ABSENT("F", "Falta"),
    JUSTIFIED("FJ", "Falta justificada"),
}

@Composable
fun GAttendanceToggle(
    option: GAttendanceOption,
    isRecorded: Boolean,
    onSelect: (GAttendanceOption) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable
fun gAttendanceRowColor(isRecorded: Boolean): Color
```

Wraps `GSegmentedPicker` with the four fixed statuses. Tokens: see
`system.md`, "GAttendanceToggle" (Component rules), plus `colorScheme.outline`
for the dashed outline drawn while `isRecorded` is false.

Tradeoffs:

- **The control owns `GAttendanceOption`, not the domain's `AttendanceStatus`.**
  This catalog knows no domain type; the calling screen maps the two, so
  `core:ui` keeps depending on nothing but the theme. The four segments and
  their `contentDescription`s still live here exactly once.
- **`gAttendanceRowColor` ships with the toggle.** The dashed outline and the
  warm row behind it are one visual state, so both read `isRecorded` from this
  file; a screen paints the row with this helper rather than branching on the
  flag itself.
- **`isRecorded` is a separate parameter, not a nullable option.** The row must
  show "present" while storing nothing (US 25, 26). Modelling that as
  `status: AttendanceStatus?` would let a caller render an empty row, which is
  never correct.
- **An unrecorded toggle fills no segment and carries a dashed outline.** The
  earlier sketch here said "a lighter selected segment"; the reviewed attendance
  design replaced it, because a filled segment reads as a decision the Teacher
  never made. The default still lives in the state (`status` is `PRESENT`), so
  the first tap on `P` records Present like any other tap, and the screen pairs
  the dashed outline with `gAttendanceRowColor` behind the whole row and an
  "N sin marcar" counter in the header.
- **`onSelect` is not nullable** — unlike `GLevelPicker`. An attendance record
  cannot be cleared back to "not recorded"; the four options are total, so
  re-tapping the selected segment re-records the same one. Making
  the two controls differ here is deliberate, and the type says so.
- **Four segments across 360dp** leaves roughly 78dp each, comfortably above the
  floor. That is the whole reason "falta justificada" is abbreviated to `FJ` with
  a full `contentDescription` rather than wrapped to two lines.

### GCheckRow

```kotlin
@Composable
fun GCheckRow(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    subtitle: String? = null,
    isEnabled: Boolean = true,
)
```

Wraps `Row` + `Checkbox`. Tokens: `GemaSpacing.medium`, `GemaSpacing.minimumTouchTarget`
row height floor, `gemaTypography.bodyLarge`, `gemaTypography.labelSmall` for `prefix`.

`prefix` carries the SIAGIE ordinal ("01", "05"), which is how a Teacher matches
a competency to a template column. The whole row is the hit area, not just the
box.

### GSwitchRow

```kotlin
@Composable
fun GSwitchRow(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
)
```

Wraps `Row` + `Switch`. Tokens: as `GCheckRow`.

Tradeoff: a switch rather than a second checkbox variant, because Areas are a
persistent on/off configuration and Worked Competencies are a selection within a
Period. The controls differ because the meanings differ; using one for both
would make "hide an Area forever" look like "tick this for now".

### GStepHeader

```kotlin
@Composable
fun GStepHeader(
    step: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
)
```

The content header used by a setup step that has no top bar: "Paso N de M"
above a title, then a one-line description. Shared by `SetupYearScreen` and
`SetupSectionScreen`.

### GChoiceChipRow

```kotlin
data class GChoiceChipOption<T>(val value: T, val label: String, val contentDescription: String)

@Composable
fun <T> GChoiceChipRow(
    options: List<GChoiceChipOption<T>>,
    selected: T?,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
)
```

A row of separate rounded chips, one per option, equally weighted. The
selected chip fills with `primaryContainer` and a 2dp `primary` border; the
rest are outlined. Differs from `GSegmentedPicker`, which renders one joined
bar — use this when the mockup shows distinct chips with a gap between them,
as the grade picker does.

### GBanner

```kotlin
enum class GBannerTone { INFO, WARNING, ERROR }
enum class GBannerActionStyle { BUTTON, LINK }

@Composable
fun GBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: GBannerTone = GBannerTone.INFO,
    icon: ImageVector? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionStyle: GBannerActionStyle = GBannerActionStyle.BUTTON,
)
```

`icon` renders a leading glyph when the mockup calls for one; omitted by
default so existing banners are unaffected. `actionStyle` picks how the
action renders: `BUTTON` (default, a full-width text button, for a dismissal
like "Entendido") or `LINK` (an inline text-plus-chevron row that navigates,
for an affordance like "No dicto todas las áreas ›").

Wraps `Surface`, radius `GemaShapes.control` (8dp), no border. Tokens:
`colorScheme.surfaceContainerLow` (INFO), `colorScheme.errorContainer` /
`onErrorContainer` text (ERROR), `GemaAccents.warningContainer` /
`GemaAccents.onWarningContainer` text (WARNING) — #163: today's code uses
`onSurfaceVariant` for the WARNING text — `gemaTypography.bodyLarge`.

Tradeoff: banners are inline in the content, never floating snackbars, for
anything that matters. A snackbar disappears after four seconds; a Teacher who
put the phone down mid-lesson would never see "Faltan 2 conclusiones". Snackbars
stay only for pure acknowledgements ("Respaldo creado").

Deviation from an earlier draft of this catalog: WARNING used to resolve to
the same neutral surface as INFO with only a different leading glyph. The
Registro tokens give WARNING its own `GemaAccents.warningContainer` tint (the
same one `GAttendanceToggle`'s unrecorded row uses), because a pending-attendance
banner and a backup-overdue banner should not read identically at a glance —
INFO stays neutral, WARNING and ERROR are now visibly distinct container
tones.

### GEmptyState

```kotlin
@Composable
fun GEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
)
```

Wraps a centred `Column`, bordered with `GemaShapes.container` (12dp) and a
hairline, no illustration. Tokens: `gemaTypography.titleMedium` / `bodyLarge`,
`colorScheme.onSurfaceVariant`, `GemaSpacing.extraLarge`.

No illustration and no mascot — the rule file forbids it, and an empty state
here is almost always one tap from being resolved. The action text is the whole
point: "Marca las competencias que trabajaste" routes straight to
`WorkedCompetencies`.

Deviation from an earlier draft of this catalog: `message` is required, not an
optional `description` — an empty state with a title and no explanation gives
the Teacher a dead end, so the component does not allow omitting it. The
action-label parameter is named `actionLabel`, matching the "label describes
the button text" convention used elsewhere in this catalog (`GBottomSheet`'s
`title`/`subtitle`), not `actionText`.

### GDialog

```kotlin
@Composable
fun GDialog(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
    isDestructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
)
```

Wraps `AlertDialog`. Tokens: `GemaShapes.container`, `colorScheme.surface`,
`colorScheme.error` for the confirm button when `isDestructive`.

`content` is a slot rather than a `message: String` because both real uses show a
list — what deleting a Section destroys, what restoring a Backup replaces. A
confirmation that names quantities is a confirmation; "Estas seguro?" is not.

### GBottomSheet

```kotlin
@Composable
fun GBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
)
```

Wraps `ModalBottomSheet`. Tokens: `GemaShapes.container`, `colorScheme.surface`,
`GemaSpacing.screenGutter`.

Tradeoff: the Period Level editor is a sheet rather than a screen so the grid
stays visible behind it and the Teacher keeps their place in a thirty-row list.
The cost is that the sheet must scroll internally when the evidence list is
long; `content` is scrollable and the level pickers are pinned above it.

### GDropdownPicker

```kotlin
@Composable
fun <T> GDropdownPicker(
    options: List<GPickerOption<T>>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
)

data class GPickerOption<T>(
    val value: T,
    val label: String,
    val badge: String? = null,
)
```

Wraps `ExposedDropdownMenuBox`. Tokens: `GemaShapes.control`, inherited field
tokens, `gemaTypography.labelSmall` for `badge`.

Used where the option count is large or variable — nine Areas, three or four
Periods, twelve months — which is exactly where `GSegmentedPicker` stops fitting.
`badge` carries "ACTUAL" on the current Period so the Teacher is never guessing
which one they are editing (US 5).

### GSearchField

```kotlin
@Composable
fun GSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
)
```

Wraps `BasicTextField` in a `Surface`, with a leading search icon and a
clear action — `OutlinedTextField`'s label padding cannot hit the mockup's
48dp height. Tokens: `GemaSpacing.minimumTouchTarget` (48dp height),
`GemaShapes.control` radius, `colorScheme.surface` fill, 1dp `outline`
border. `pill` is reserved for `GBadge`; this control no longer uses it.

Wave 2: Students sits outside the Phase 4 screen waves (#168/#169/#170), so
this token swap lands with the Students screen's own ticket, not Phase 4.

It is a separate component from `GTextField` only because the clear affordance
is a search convention, and because filtering is local and instant — there
is no debounce parameter, since there is no query to throttle.

---

### GExtendedFab

```kotlin
@Composable
fun GExtendedFab(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

Wraps `ExtendedFloatingActionButton`. The primary creation action for a list
screen (Students, Sections, School Years, Activities) — floats bottom-end via
`GScreen`'s `fab` slot instead of sitting in the top bar. `fab` and
`bottomAction` are mutually exclusive on `GScreen`. Tokens:
`GemaSpacing.fabHeight` (52dp — #163: today's code ships 56dp),
`GemaShapes.control` (8dp) radius, `colorScheme.inverseSurface`
fill with `colorScheme.inverseOnSurface` icon and label.

Deviation from an earlier draft of this catalog: the fab moves from
`primaryContainer` to `inverseSurface`. It reads as one dark object on a
white screen — always the same shape, always the same tone — rather than a
tinted button competing with the primary green used elsewhere on the screen.

---

### GGroupHeader

```kotlin
@Composable
fun GGroupHeader(
    title: String,
    count: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

Wraps `Surface` + `GText`. A tinted, full-bleed, tappable divider between a
list's primary rows and a collapsed secondary group — Students' withdrawn
roster today. Tokens: `GemaSpacing.compactRowHeight`, `surfaceContainerLow`
background / `onSurfaceVariant` text, `surfaceContainerHigh` pressed state,
`GTextStyle.LABEL_SMALL_EMPHASIS`.

Wave 2: Students sits outside the Phase 4 screen waves (#168/#169/#170), so
this token swap lands with the Students screen's own ticket, not Phase 4.

---

### GBadge

```kotlin
enum class GBadgeTone { PRIMARY, ERROR }

@Composable
fun GBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: GBadgeTone = GBadgeTone.PRIMARY,
    onClick: (() -> Unit)? = null,
)
```

Wraps `Surface` + `Text`. A small pill-shaped label for a short piece of
state (a count, a status word); `onClick` makes it tappable, otherwise it is
a static tag. Tokens: see `system.md`, "GBadge" (Component rules).

---

### GMonthPickerDialog

```kotlin
@Composable
fun GMonthPickerDialog(
    value: YearMonth,
    onConfirm: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    minimum: YearMonth? = null,
    maximum: YearMonth? = null,
)
```

Wraps `GDialog` with a year stepper (`GIconButton` pair around a `GText` year
label) and a 3×4 grid of month chips using `MonthNames.short`. Tapping a chip
selects it without closing the dialog; `GDialog`'s confirm button commits the
selection. Months outside `[minimum, maximum]` are disabled and dimmed to
`outlineVariant`, and the year stepper cannot cross into a year with no
in-bounds month — the attendance month screen passes
`maximum = YearMonth.now()` for "no future months", the same rule its own
stepper's forward arrow already enforces.

A 12-cell grid rather than Material3's day-grid `DatePicker` (the one
`GDateField` wraps): picking a month from a calendar of days is the wrong
mental model, and a day grid cannot express a month-only bound cleanly.

### MonthNames

`core.ui.MonthNames.short` is the twelve lowercase three-letter Spanish month
abbreviations (`ene` … `dic`), used by `GMonthPickerDialog`. It duplicates the
private list `feature/attendance/AttendanceLabels.kt` already has because that
one is feature-scoped and `core:ui` cannot depend on a feature module; #86
(moving these labels to string resources) is the ticket to collapse both into
one source.

---

## What is deliberately not in the catalog

- **No general `GTable`.** `GTableHeaderBand`/`GTableRow` are a header-band
  and row shell, not a full grid component — they own no columns, no pinned
  name column and no horizontal scroll. The Period Levels grid's pinned-column,
  scrollable layout is still specific to it and lives in the evaluation
  feature package; `GLevelChip` is the shared part there.
- **No `GLoadingIndicator`, `GSkeleton` or `GShimmer`.** Every read is a local
  database read. A designed loading state for a query that resolves in one frame
  would be a flash, and shimmer placeholders are pure recomposition cost on a
  device that has none to spare. The three genuinely slow operations (backup,
  restore, export) use `GButton(isBusy = true)`.
- **No `GAvatar` or `GInitialsCircle`.** There are no photos, and coloured
  initials in a list of thirty children is decoration with a memory cost.
- **No `GSnackbarHost` variants.** One host lives in `GScreen`; anything that
  must survive being ignored is a `GBanner`.
- **No `GTabRow`.** Areas number nine and Periods three or four; both are
  `GDropdownPicker`. Tabs would either overflow horizontally or force truncated
  labels.
- **No theme toggle or dark-mode switch.** The theme follows the system. A
  hand-rolled toggle is a setting to maintain and a second palette to keep
  correct, for no offline benefit — though every component must still render
  correctly in dark mode, and every one ships a `@PreviewLightDark` preview as
  the rule file requires.
