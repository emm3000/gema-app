# Registro design system

## Status

Approved 2026-09-13. This document is the source of truth for Phase 4
implementation waves.

Canvases:

- Identity (seven principles): https://claude.ai/code/artifact/ce4bb182-e374-40d9-96f1-3cabda03921d
- System (tokens and components): https://claude.ai/code/artifact/d818e6b3-1f76-4775-9373-ea7e0dbe9be6
- Screens (Home, Attendance day, Period levels, Level sheet): https://claude.ai/code/artifact/e3d89b45-16c4-4e88-9fd5-5d32a1db61f3

## Principles

Seven principles decide every token, component and screen that follows.

1. **The teacher's own tool** — Independent, personal, on one device. SIAGIE
   compatibility is a feature at export time, never the visual identity.
2. **Today first, one tap away** — Daily screens lead with today's task. The
   primary action sits in the thumb arc. Every target is at least 48dp.
3. **A level is never a color** — AD, A, B and C are always text or shape.
   Color is reserved for state: absent, unmarked, backup overdue.
4. **Legible in the schoolyard** — Contrast above the minimum, big numerals
   for counts, and layouts that survive font scale 1.3 on a 360dp screen.
5. **Nothing is lost** — Every tap persists immediately. A screen interrupted
   by a child resumes exactly where it was.
6. **Quiet by default** — Whitespace, type and hierarchy carry the identity.
   Fewer cards, fewer borders, fewer colors, no decoration.
7. **Dark mode is correct, not designed** — The palette must hold up in dark
   mode; it is not styled separately and there is no toggle.

## Color tokens

Light scheme drives design; the dark column exists for contrast compliance
only — it is correctness-only, not a designed alternate theme.

| Role (Color.kt) | Light | Dark | Use |
|---|---|---|---|
| `background` / `surface` | `#FFFFFF` | `#191A1C` | Screen ground. Lists sit directly on it. |
| `surfaceContainerLow` | `#F6F6F4` | `#222326` | Banners, summary strips, grid header. The only tinted surface. |
| `surfaceContainerHigh` | `#EDEDEA` | `#2C2D31` | Pressed rows, sheet handle, current-row highlight. |
| `onSurface` | `#17181A` | `#ECECEA` | Ink. Titles, names, levels, numerals. |
| `onSurfaceVariant` | `#6E7075` | `#A0A2A8` | Muted. Subtitles, eyebrows, icons at rest. 4.7:1 on white. |
| `outlineVariant` | `#E6E6E3` | `#34353A` | Hairlines between rows. 1dp, never thinner. |
| `outline` | `#C9C9C5` | `#55575D` | Control borders: secondary button, picker, empty level cell, toggle. |
| `primary` | `#3F6836` | `#A5D396` | Brand. The one primary action per screen, the current marker, links. |
| `primaryContainer` + `onPrimaryContainer` | `#E4EFD9` / `#285020` | `#2C4A27` / `#C0EFB0` | Selected present, selected level chip, primary badge. |
| `error` | `#B42318` | `#F4A29A` | State, never a grade: incomplete cell border, destructive button, backup dot. |
| `errorContainer` + `on` | `#FDE1DF` / `#8C1D13` | `#5C1E19` / `#FDE1DF` | Absent segment fill, error banner. |
| `warningContainer` + `on` (`GemaAccents`) | `#FBF4E1` / `#5C4A12` | `#3A3222` / `#EAD79C` | Pending attendance row, warning banner. |
| `inverseSurface` | `#17181A` | `#ECECEA` | Justified-absence segment, snackbar, extended FAB. |

Note: dark `onPrimaryContainer` (`#C0EFB0`), `onErrorContainer` (`#FDE1DF`)
and `onWarningContainer` (`#EAD79C`) are the designer-approved dark
foregrounds against their unchanged containers — contrast 7.66:1, 10.28:1
and 8.86:1 respectively, all comfortably above WCAG AA.

Note: `GemaAccents.unmarkedSurface`/`onUnmarkedSurface` and
`absentContainer`/`onAbsentContainer` are deleted, not renamed, in #163.
`GAttendanceRow` moves to the `warningContainer`/`onWarningContainer` pair
above (unmarked) and `colorScheme.errorContainer` (absent).

Note: secondary and tertiary tonal families are removed. Nothing on the three
daily screens used them with intent.

Note: dark mode is correctness-only — it exists so contrast stays compliant
in a dark environment, not as a designed alternate theme. There is no theme
toggle.

## Typography

IBM Plex Sans, bundled as a variable TTF. Width axis (`wdth`) pinned to 100,
weight range 400-600, Latin and Spanish subset, all layout features kept:
119 KB measured (106 KB unhinted). Fits the 300 KB typeface budget with room.

| Style | Size / weight / line-height | Use |
|---|---|---|
| `headlineSmall` | 24sp / 600 / 1.2 | Screen titles |
| `titleLarge` | 20sp / 600 / 1.3 | Card and section names |
| `titleMedium` | 16sp / 500 / 1.4 | List titles, student names |
| `bodyLarge` | 15sp / 400 / 1.45 | Body, subtitles |
| `bodySmall` | 13sp / 400 / 1.45 | Captions, helper text |
| `labelLarge` | 15sp / 500 | Buttons, segments |
| `labelSmall` | 12sp / 600, +1px tracking | Eyebrows |
| `bodyMedium` | 15sp / 400 / 1.45 | Registro value, mirrors `bodyLarge`. M3 `ListItem` supporting text, `Dialog` body |
| `titleSmall` | 12sp / 600, +1px tracking | Registro value, mirrors `labelSmall`. M3 components that read `titleSmall` directly |
| `labelMedium` | 12sp / 600, +1px tracking | Registro value, mirrors `labelSmall`. `GBadge` text, `DatePicker` |

`gemaTypography` keeps these seven `GTextStyle` slots. It also sets
`bodyMedium`, `titleSmall` and `labelMedium` to Registro values, because
about 15 `core:ui` components and M3's `ListItem`, `Dialog` and `DatePicker`
read those three slots directly, not through `GTextStyle`. The `GTextStyle`
entries `BODY_MEDIUM`, `TITLE_SMALL` and `LABEL_MEDIUM` are dropped; every
`GText` call site remaps:

- `BODY_MEDIUM` becomes `BODY_LARGE` (15sp / 400 / 1.45).
- `TITLE_SMALL` becomes `TITLE_MEDIUM` (16sp / 500 / 1.4) when it titles a
  list or a month, or `LABEL_SMALL` as an eyebrow when it heads a control
  group.
- `LABEL_MEDIUM` becomes `LABEL_SMALL` (12sp / 600, +1px tracking, color
  `onSurfaceVariant`).

`GTextStyle.NUMERAL` is not a `Typography` slot: `GText.kt` resolves it
directly as `titleLarge.copy(fontWeight = FontWeight.SemiBold,
fontFeatureSettings = "tnum")` — 20sp / weight 600 / lineHeight 26sp, tabular
figures (`tnum`), color `onSurface`. It replaces `CARD_TITLE_EMPHASIS`,
`gemaCardTitleFontSize` and `gemaCardDateFontSize` (#163 scope). Every size
must survive font scale 1.3 on 360dp: rows grow, nothing truncates a name to
one line.

## Spacing

Unchanged from the current token set:

- `extraSmall` 4dp — icon to label, dot to text
- `small` 8dp — inside a row, between chips
- `medium` 16dp — row padding, `screenGutter`
- `large` 24dp — between groups
- `extraLarge` 32dp — before a section eyebrow
- `minimumTouchTarget` 48dp — unchanged, every tappable thing
- `gridRowHeight` 56dp — unchanged · `gridCellWidth` 56dp · `gridChipHeight` 44dp

## Shape

- `control` 8dp — buttons, inputs, chips
- `chip` 6dp — evidence chips
- `container` 12dp — cards, sheets
- `pill` — badges

`hairline` stays 1dp; `activeBorder` (the current marker) stays 2dp. Radii
come down from the previous 12 / 8 / 16 set to 8 / 6 / 12: quieter corners,
same roles. Cards lose their tint: a card is white with a hairline border, no
color tint — use a row (`GListItem`) when the content is one line.

## Component rules

**GTopBar** — title, subtitle, `onBackClick`, actions. White, no elevation, a
hairline only appears once content scrolls under it. Tokens: `titleMedium`
for title, `labelSmall` + `onSurfaceVariant` for subtitle.

**GButton** — 48dp height, `control` radius, `labelLarge`. Variants PRIMARY
(`primary` fill), SECONDARY (`outline` border), TEXT (underline, no fill),
DESTRUCTIVE (`error` border and text). `isBusy` dims to 85% opacity with a
spinner; disabled uses `surfaceContainerHigh` fill and `onSurfaceVariant`
text.

**GIconButton** — 48dp target, 22dp glyph, 1.8dp stroke. Disabled state uses
`outline`.

**GCalendarIconButton** — same token set as `GIconButton`; renders the
calendar glyph for the date-stepper's picker affordance.

**GOverflowMenu** — 220dp wide dropdown, `outlineVariant` border, `container`
radius, 48dp rows, `labelLarge`.

**GExtendedFab** — moved from `primaryContainer` to `inverseSurface`: one
dark object on a white screen, always the same thing. `GemaSpacing.fabHeight`
52dp (#163 — today's code ships 56dp), `GemaShapes.control` (8dp) radius,
`colorScheme.inverseOnSurface` icon and label.

**GBadge** — pill shape, PRIMARY tone (`primaryContainer` / `onPrimaryContainer`)
or ERROR tone (`errorContainer` / `on`), 32dp visual height inside a 48dp
touch target when clickable.

**GBanner** — INFO (`surfaceContainerLow`), WARNING (`GemaAccents.warningContainer`
/ `GemaAccents.onWarningContainer` text — #163: today's code uses
`onSurfaceVariant` for this text), ERROR (`errorContainer` / `on`). `control`
radius (8dp), no border. Optional leading dot for the ERROR/backup-overdue
case, optional trailing link action.

**GListItem** — sits directly on the screen ground, separated by one
`outlineVariant` hairline. Leading label 28dp wide, tabular. Names wrap to
two lines at font scale 1.3 instead of truncating. Pressed state uses
`surfaceContainerHigh`.

**GCard** — white, `container` radius (12dp), one hairline border,
`containerColor` defaults to `surface`, never `surfaceVariant`. No color
tint. Use a `GListItem` row instead when the content is one line.

**GBorderedContainer** — same hairline + `container` radius rule as `GCard`,
for a non-card grouping surface (grid, sheet body).

**GDropdownPicker** — 48dp control, `outline` border, `control` radius.
Expanded menu matches `GOverflowMenu`'s surface treatment, with an optional
PRIMARY badge ("Actual") on the current option.

**GEmptyState** — title, message, `actionLabel`. Centered, `container`
radius border, no illustration.

**GAttendanceToggle** — four 48dp segments (`P` / `T` / `F` / `FJ`) in one
`outline`-bordered, `control`-radius row. Selected segment = fill + weight
600: present on `primaryContainer`, late on `surfaceContainerHigh`, absent on
`errorContainer`, justified on `inverseSurface`. The unrecorded row: dashed
`outline` border drawn on top of the segments, `surface` (white) fill,
trailing "sin marcar" label in `GemaAccents.onWarningContainer` text (#163:
today's code uses `onSurfaceVariant`). Color is always redundant with the
letter and the weight, never the only signal.

**Attendance summary strip** — `surfaceContainerLow` background, `control`
(8dp) radius, `numeral` count + `bodyLarge` label, a secondary "sin marcar"
line in `GemaAccents.onWarningContainer` text (#163: today's code uses
`onSurfaceVariant`). "Todos presentes" renders as a SECONDARY `GButton`.

**GLevelChip states** — GRID (48×44dp inside a 56dp row), INLINE (48dp
height), EVIDENCE (34×26dp, `chip` radius). Default: `outline` border,
`onSurface` letter, weight 600, no fill. `isCurrent`: 2dp `primary` border.
`isIncomplete`: `error` border plus a small "!" marker. Incomplete wins over
current when both are true. **Invariant, verbatim: a level letter is always
onSurface; only isIncomplete uses error.**

**Level grid row** — pinned name column 150dp, cells 56dp wide, row 56dp,
header on `surfaceContainerLow`.

**GLevelPicker** — sheet row of 52dp chips, flex 1, `control` radius.
Selected = 2dp `primary` border + `primaryContainer` fill; the letter itself
stays `onSurface` ink in every chip, selected or not — selection is state,
the letter is not.

## Phase 4 waves

1. #163 Tokens
2. #164 Typeface
3. #165/#166/#167 Components (parallel)
4. #168/#169/#170 Screens (parallel)

No wave changes a `UiState` contract unless `screens.md` explicitly says so.
