# Redesign brief

Status: working brief, 2026-09-12, section 6 questions answered 2026-09-13. Input for the design and UX cycle that starts next. Every claim cites a file, or is labelled **Hypothesis** or **Decision**. Terminology follows `CONTEXT.md`.

## 1. Problem statement

The owner's assessment: the app looks basic and has no identity of its own.

The code supports a narrower version of that.

- **A design system exists, as engineering scaffolding.** There are theme tokens in `core/ui/src/main/kotlin/com/emm/gema/core/theme/`, a `G*` component catalog (`docs/design/components.md`, 47 `G*`-prefixed files under `core/ui/src/main`), per-screen specs with `UiState` and intents (`docs/design/screens.md`, 21 screens), a navigation map (`docs/design/flows.md`), 23 approved HTML mockups (`docs/design/mockups/`), and the `G*`-only rule (`.claude/rules/ui-components.md`), which every review enforces.
- **Design direction is missing.** No document says why the app looks the way it does, what it should feel like, or which information a daily screen should put first. The existing rationale covers constraints and prohibitions (`flows.md`, "Design constraints that shape every flow"; `components.md`, `GLevelChip` tradeoffs). It does not describe a personality.
- **The visual identity is generated, not designed.** STEP 1 refines the summary here. A custom color palette does exist, so the app is not on Material3 baseline colors. But the palette reads as a Material Theme Builder tonal scheme built from one green seed (`#3F6836`). The type scale is the Material3 default, unchanged. See section 7.
- **The gap is in the design source, not in the implementation.** The mockups use the same hex values as `Color.kt` (for example `#3F6836`, `#191D17`, `#F8FBF1` in `mockups/home.html`) and the Roboto/system sans stack. `screens.md` says the mockup wins over the wireframe. After the drift wave, the shipped app matches the mockups closely (`docs/design/reviews/*-after.png`). Polishing the implementation cannot add an identity the mockups never had.

## 2. The user

**Facts** (`CONTEXT.md`, `CLAUDE.md`):

- The Teacher is a Peruvian primary teacher (EBR, grades 1-6) and the app's single user. There is no login and no backend, and the device is the only source of truth.
- The app records Attendance and Achievement Levels under the CNEB. The literal scale is AD / A / B / C, and a level is never averaged or computed (`docs/cneb/primary.json`, ADR 0003).
- Export fills the Teacher's own imported SIAGIE Template. The shipped interface language is Spanish (`flows.md`).

**Usage context: hypotheses, all unvalidated.** `flows.md` already states several of these as settled constraints. No teacher has confirmed any of them.

| # | Hypothesis | Design consequence if true |
|---|---|---|
| H1 | Low-end Android devices, 360dp and 4.5"-5.5" screens | Density and performance budget stay tight |
| H2 | Attendance is taken with about 30 children present | The Attendance Day row must be scannable and one tap per Student |
| H3 | One-handed use, standing, between classes | Primary actions sit in the thumb arc |
| H4 | Bright-light or outdoor use happens | Contrast must go beyond the minimum, and color is never the only signal |
| H5 | Attention is intermittent, with frequent interruptions | Every screen resumes without losing context, and every tap persists |

The closed alpha, now in Google review, will be the first real evidence for or against H1-H5.

## 3. What the architecture makes cheap, and what it makes expensive

**Cheap: retheming.** Feature screens may call only `G*` components and may not hold literal colors, sizes or radii (`.claude/rules/ui-components.md`). Color, typography, shape and spacing therefore live in `Color.kt`, `Type.kt`, `Foundation.kt` and `Theme.kt`, and a change there reaches every screen. The one exception is component internals: a component that picks a particular `colorScheme` role, such as `GLevelChip`'s neutral surface, changes in its own `G*` file, which is still inside `core:ui`.

**Expensive: information hierarchy and flow.** Changing what a screen shows first, or how screens connect, touches the matching section of `screens.md`, its mockup, `flows.md`, and the feature's `Screen` composable. For Home, Attendance and Evaluation, it can also reach `UiState` shape and ViewModel tests.

**Governing constraint.** `CLAUDE.md`, "Rebuild, never adapt": when the current design does not fit the new direction, the implementation replaces it cleanly, with no compatibility layer, no second theme alongside the first, and no per-screen overrides on top of old tokens. The same rule applies to the design sources: redesigned specs and mockups replace the current ones rather than sit next to them.

## 4. Proposed cycle

**Decision (proposed):** four phases, each closed by an explicit owner approval gate. **No production Compose code changes until Phase 3 is approved.**

### Phase 1: Principles and identity

Decide who the user is (section 2, restated as design input), what the app should feel like, its tone of voice in Spanish, color with meaning, typography and density. The color decision must say how the AD / A / B / C scale is represented. Today it is one neutral surface carrying the letter, and color is forbidden for grades (`components.md`, `GLevelChip`; `.claude/rules/ui-components.md`). Phase 1 either reaffirms that rule with a stated reason or replaces it explicitly. It must not erode it quietly.

Phase 1 also decides, in words and before anything is drawn, what each daily screen must show (section 5, scope risk).

Output: a written design principles document.
Gate: owner approves the principles.

### Phase 2: Tokens and components

Redesign the `core:ui` token set (palette, type scale with intent, shape, spacing, density) and the `G*` catalog on top of Phase 1. Rewrite `components.md` to match the code. It currently disagrees with the code (section 7).

Output: the token spec and the revised catalog, shown on a canvas. No Compose yet.
Gate: owner approves tokens and components.

### Phase 3: Key screens first

Redesign the daily-use screens first: Home, Attendance (AttendanceDay, AttendanceMonth) and Evaluation (PeriodLevels, PeriodLevelSheet, ActivityEvidence). The remaining screens follow.

Output: redesigned `screens.md` sections and mockups that replace the current ones.
Gate: owner approves the key screens. Production Compose work may start only after this gate.

### Phase 4: Implementation in waves

Tokens and components land first, then screens, in small PRs. Each PR goes through the same two-axis review in use today (standards and spec) and a per-screen visual check against the new mockups.

### Tooling for Phases 1-3

- A design canvas that produces multi-artboard mockups the owner can refine by hand. This is preferred over an agent redesigning directly in Compose, because it keeps design iteration cheap and outside the codebase until Phase 3 approves it.
- **Owner suggestion:** use the Claude Fable model for design work.
- **Caveat on record:** the choice of model matters less than the quality of the brief. Without the user context in section 2, any model produces generic polish.

## 5. Constraints and risks

**Timing.** The closed alpha is in Google review. **Decision (owner, earlier):** Play Store screenshots are not replaced until the alpha clears review. Design work running in parallel does not conflict with that decision. Shipping a redesign does, because it would invalidate the store listing assets in `docs/play/assets/`: four screenshots, the feature graphic and the icon.

**Accessibility is a floor, not a style.** A redesign must not regress:

- the 48dp minimum touch target, `GemaSpacing.minimumTouchTarget` (`Foundation.kt`);
- font scaling up to at least 1.3. `GDateField` regressed on this twice (commits `2974547`, `c33db52`, `445e984`; issue #155);
- per-option accessibility semantics. `GAttendanceToggle` lost them silently in PR #136 (ADR 0024).

The Robolectric Compose tests from ADR 0024 (`HomeScreenTest`, `GAttendanceToggleTest`, `GDateFieldTest`, `ExportScreenTest`) catch some of these regressions, not all. They assert no color, contrast or spacing, so contrast (H4) needs its own check during the redesign.

**Test cost.** Owner-reported figure, from today's CI run: the Unit tests step rose from about 12.6s to about 52s across the four Robolectric Compose classes. ADR 0024 sets roughly 60s of added Robolectric time as the point to revisit the approach. A redesign that adds a Compose test class per screen reaches that point quickly. Prefer fewer, denser classes, as the ADR suggests.

**Scope risk.** Redesigning information hierarchy is a product decision, not a visual one. The product rules in `flows.md` (nothing is ever computed; Import and Export are previewed before they act) stay binding. Phase 1 must decide what each daily screen shows before anything is drawn, or Phase 3 becomes an unbounded product redesign.

## 6. Open questions for the owner (start of Phase 1)

1. Does any brand material exist (name treatment, logo beyond `docs/play/assets/icon.svg`, colors, typeface), or does Phase 1 create the identity from nothing?
2. Should Gema feel institutional, aligned with MINEDU and SIAGIE, or independent, a teacher's own tool?
3. Is dark mode in scope as a designed palette, or only as a correctness requirement, which is today's position (`components.md`, "No theme toggle")?
4. Does the rule that color never carries an Achievement Level stay?
5. Are real teachers available to test H1-H5 before Phase 3, and if not, which alpha signals count as validation?
6. May the new typography ship a bundled typeface, given its APK size cost on low-end devices (H1), or must it stay on the system sans?

### Answers (owner, 2026-09-13)

1. No brand material beyond `docs/play/assets/icon.svg`. Phase 1 creates the identity from scratch, with the current icon as the anchor.
2. Independent: a teacher's own tool. Reference feel: Notion-like, clean, sober, generous whitespace. SIAGIE compatibility is a feature, not the identity.
3. Dark mode stays a correctness requirement only. No theme toggle, no separately designed dark palette.
4. The rule stays: color never carries an Achievement Level.
5. No real teachers before Phase 3. The alpha signal is direct conversation with the closed-alpha testers; without it, H1-H5 remain hypotheses and the design stays conservative.
6. One bundled variable typeface is allowed, with an APK budget of 300 KB; above that, the type system stays on the system sans.

## 7. Appendix: current state (STEP 1 facts)

**Color:** `core/ui/src/main/kotlin/com/emm/gema/core/theme/Color.kt`, `Theme.kt`

- A full custom Material3 light and dark scheme covers every `colorScheme` role, including the surface-container roles. Primary is `#3F6836` (light) and `#A5D396` (dark), secondary `#54634D`, tertiary `#386568`, and the backgrounds are green-tinted (`#F8FBF1`). The structure and tones match Material Theme Builder output from a single green seed. It is a real palette, but it was not designed with intent.
- Three app-specific accent pairs are exposed through `GemaAccents` (`Foundation.kt`): `unmarked`, `warningContainer` (the same values as `unmarked`) and `absentContainer` (the same values as `errorContainer`).
- `GemaTheme` supports dynamic color and defaults it to `false`. The theme follows the system dark setting. No `Shapes` are passed to `MaterialTheme`.
- No color token exists for Achievement Levels. This is deliberate (`components.md`, `GLevelChip`).

**Typography:** `Type.kt`

- `gemaTypography` copies the Material3 default `Typography()` for all 15 roles and changes only the font family, to `FontFamily.SansSerif` for both display and body. Sizes, weights and line heights are the Material3 defaults.
- There are two loose sizes outside the scale: `gemaCardTitleFontSize` at 20sp and `gemaCardDateFontSize` at 18sp.
- No bundled typeface.

**Shape and spacing:** `Foundation.kt`

- `GemaShapes`: `chip` 8dp, `control` 12dp, `container` 16dp, `pill`. `GemaBorder.hairline` is 1dp.
- `GemaSpacing` has a 4/8/16/24/32dp scale, `screenGutter` 16dp and `minimumTouchTarget` 48dp, plus about 14 component-specific dimensions (grid cells, chips, FAB height, and others).

**Component catalog:** `docs/design/components.md`, `core/ui/src/main/kotlin/com/emm/gema/core/ui/`

- The catalog table lists 31 components. The prose says "sixteen" and "six planned", and marks `GAttendanceToggle` as planned, but a test for it exists (`GAttendanceToggleTest`). The token vocabulary names `xs`/`sm`/`minTouchTarget`, `GemaColors`, `GemaTypography` and `numericMedium`, and none of those exist in the code, which uses `extraSmall`/`small`/`minimumTouchTarget` and `gemaTypography`.
- The catalog documents deliberate exclusions: no loading skeletons, no avatars, no tabs, no theme toggle.

**Specs and mockups**

- `docs/design/screens.md`: 21 screens with ASCII wireframes, `UiState` and intents. The HTML mockup wins over the wireframe. Twelve open product questions are listed at the end.
- `docs/design/flows.md`: the navigation map, plus the constraint table that today acts as the only statement of design rationale.
- `docs/design/mockups/`: 26 HTML mockups with PNGs (historical, as of this writing; Phase 3 shipped four Registro mockups — home, attendance day, period levels and period level sheet — and the setup-and-sections batch added seven more plus two dialogs, and the students-and-gates batch added five more plus the import rejection state, all updated in place and with their stale PNGs removed pending Phase 4 regeneration). `alternative-b-attendance-grid` and `alternative-c-levels-per-student` are explorations, not accepted designs. `docs/design/reviews/` holds 5 after-screenshots from the drift wave.

**Rules:** `.claude/rules/ui-components.md`

- Features use `G*` only and never raw Material3.
- The theme files are the only style guide, and no separate hex-code document may exist. The Phase 1 principles document explains intent and must not duplicate token values.
- Semantic colors never carry a grade. No mascots and no emoji icons.

**Domain:** `docs/cneb/primary.json`

- CNEB primary, grades 1-6: Areas with numbered Competencies, sourced from MINEDU's 2016 curriculum. It is graded on the literal AD / A / B / C scale.
