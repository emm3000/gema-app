---
status: accepted
---
# Robolectric Compose tests, scoped to navigation dispatch

On the same day, PR #136 landed two regressions of the same class: a rewrite
of `SectionCard` dropped `GListItem`'s `onClick`, breaking documented Home
navigation, and a rewrite of `GAttendanceToggle` dropped the per-option
accessibility semantics `GSegmentedPicker` had applied, leaving a screen
reader announcing bare letters with no selected state. Both incidents share
one mechanism: a hand-rewrite of an interactive component silently loses
behaviour the component already had, while the `ViewModel` suite stays green,
because the break is in the Compose wiring and nothing in this repo could
assert that layer. `HomeViewModelTest` asserted the whole time that
`SectionClicked` produces `NavigateToSectionDetail`, and it passed the entire
time the feature was dead in the running app.

With 21 documented `NavigateTo*` destinations, each backed by a tappable
element, the exposure is repo-wide.

## What is tested, and what is deliberately not

A test asserts that tapping a documented affordance dispatches the intent the
design says it should. It does not assert layout, styling, colour or spacing
— the per-screen visual check against `docs/design/screens.md` keeps that job.

`.claude/rules/principles.md` puts YAGNI first: build for an observed
requirement, not a hypothetical one. Two same-day regressions of the same
class made the requirement observed. Robolectric-based Compose tests run on
the JVM inside the existing `testDebugUnitTest` gate, so they need no
instrumentation, no emulator provisioning and no CI change — the only choice
that adds no new infrastructure category. Instrumented (`androidTest`) UI
tests were rejected as disproportionate for a single-developer app: a second,
heavier test infrastructure owned indefinitely. The manual per-screen visual
check was rejected as the sole safeguard because it is probabilistic — it
caught #136 only because the check happened to exercise the broken element.

This is not a mandate to test Compose UI generally. Only Home's `SectionCard`
case — the one that regressed — is covered, as the reference pattern for the
next screen someone touches. Retrofitting all 21 destinations in one sitting
would produce a suite nobody who wrote it maintains; one test the next person
copies is worth more than twenty nobody reads.

The category also covers the second half of the same #136 incident:
`GAttendanceToggle` dropping `GSegmentedPicker`'s per-option accessibility
semantics. `GAttendanceToggleTest` (`core:ui`) asserts each option's
`contentDescription` and selected state through the merged semantics tree —
the same failure surface a screen reader relies on and nothing else in the
suite could assert. It is deliberately not a mandate to add a semantics test
to every `G*` component; it covers the one that regressed, alongside
navigation dispatch (`HomeScreenTest`) and enabled/disabled state
(`ExportScreenTest`, below) as one more admitted assertion kind, not a
license to test Compose UI generally.

## Where the test setup lives

`configureAndroidCompose` in `build-logic/convention` — not `gema.android.feature`
or `gema.android.compose` as two separate homes — because the regression that
prompted this, `SectionCard` in `HomeScreen.kt`, lives in `:app`, not in a
`feature:*` module. `:app` applies `gema.android.application`, which calls
`configureAndroidCompose` directly, the same function `gema.android.compose`
calls for `core:ui` and every `feature:*` module. Adding the test dependencies
and `testOptions.unitTests.isIncludeAndroidResources = true` inside that one
function reaches `:app`, `core:ui` and every feature module without adding a
second convention plugin or touching `core:database`, which applies
`gema.android.library` without Compose and has no use for Robolectric.

`androidx.compose.ui:ui-test-manifest` is wired as `debugImplementation`, not
`testImplementation`: `testDebugUnitTest` resolves against the `debug`
variant's merged manifest, and only `debugImplementation` puts that
artifact's `ComponentActivity` launcher entry into that merge. Declaring it
`testImplementation` leaves Robolectric unable to resolve a launcher activity
for `createComposeRule()`.

## Screen with a fake `onIntent`, not Route with a captured effect

`HomeScreenTest` drives `HomeScreen` directly with a fake `onIntent` lambda
and asserts the captured `HomeUiIntent`, rather than going through
`HomeRoute` and asserting the emitted `HomeUiEffect`. The Route path needs a
real `HomeViewModel` resolved through Koin (`HomeRoute`'s default parameter is
`koinViewModel()`), which means starting a Koin context and faking every
repository the ViewModel depends on — the same fakes `HomeViewModelTest`
already builds to test the ViewModel's effect mapping. Routing through
`HomeRoute` would duplicate that coverage while adding Koin test
infrastructure that buys nothing: the regression this ADR is about was never
in the ViewModel or its effects, it was in the Composable that never called
`onIntent` at all. The Screen-level test isolates exactly that failure
surface, with no DI setup, and runs faster.

## Cost

Robolectric's per-class startup — loading the Android framework jar,
inflating the merged manifest, standing up a `ComponentActivity` — is a fixed
cost per test *class*, not per assertion. A single forced run of
`:app:testDebugUnitTest --tests HomeScreenTest` measured 2.7s of JUnit-reported
test time inside a ~10s Gradle task (including JVM worker fork); a peer
review of the same run measured 13s wall-clock on different hardware. Either
number lands in the same place: this is seconds per class, and it scales with
the number of Robolectric test classes, not with how many assertions live in
them.

That means ten screens with two or three Robolectric classes each is real
minutes added to `testDebugUnitTest`, the gate that runs before every commit.
If that gate's added Robolectric time crosses roughly 60 seconds — a handful
of screens in — it is worth revisiting whether every screen needs its own
Robolectric class, or whether some of this coverage moves to fewer, denser
classes.

A local forced run of `:core:ui:testDebugUnitTest --tests GAttendanceToggleTest`
measured 2.77s of JUnit-reported test time, in line with `HomeScreenTest`'s
~2.7s — confirming the fixed per-class cost holds across `:app` and
`core:ui`. This is a local measurement, not CI-corroborated. No `NATIVE`
graphics mode is needed here either: the test reads semantics, not measured
layout.

`@GraphicsMode(GraphicsMode.Mode.NATIVE)` roughly doubles that per-class cost:
`GDateFieldTest` (issue #155), the third class, measured 6.06s against the
2.7s `HomeScreenTest` reference. Default Robolectric graphics mode returns
degenerate, near-zero-width text measurement for Compose text, which makes
`NATIVE` a requirement whenever a test asserts a real layout size or line
count — `GDateFieldTest` needed it to distinguish a field that wraps to fit
its value from one that does not. A test with no width- or layout-sensitive
assertion, like `HomeScreenTest`, does not need it and should not pay for it.

## Verification

`HomeScreenTest` was confirmed to catch the exact #136 regression: with
`SectionCard`'s `onClick` removed, the test failed because no intent was
captured; with `onClick` restored, it passed.

`GAttendanceToggleTest` was confirmed the same way, against both semantics
sources it asserts: with the `.semantics { }` modifier removed, the test
failed because no node matched the expected `contentDescription`; with only
`selectable(...)`'s `selected` argument forced to `false` (leaving
`contentDescription` in place so the node stayed locatable), the test failed
because the selected-state assertions no longer held. Restoring either change
passed again.

## Widening the category: enabled/disabled state

`ExportScreenTest` (issue #158), the fourth class, asserts something neither
navigation dispatch nor semantics: that a button's `enabled` state tracks
`ExportUiState.activeExport` correctly across all four Export actions. The
same underlying gap applies — `ExportViewModelTest` can assert
`activeExport` transitions correctly in the state, but nothing in this repo
could assert that a Composable actually wires that state into whether a
control accepts a tap. PR #159's own review caught exactly that class of
bug: the first draft carried a dead `|| activeExport == SELF` branch that
`GButton`'s `isClickable = enabled && !isBusy` already made redundant, and
only a test that inspects the rendered `enabled`/`isBusy` semantics — not a
read of the source — would have caught a *wrong* mapping the same way.

This needs no `NATIVE` graphics mode — `assertIsEnabled`/`assertIsNotEnabled`
read semantics, not measured layout — so `ExportScreenTest` cost 4.26s of
JUnit-reported test time, in the same range as `HomeScreenTest`'s 2.7s
non-`NATIVE` baseline, not `GDateFieldTest`'s 6.06s.

## Expected next step: extract the boilerplate at the third screen

`HomeScreenTest` repeats `@RunWith(RobolectricTestRunner::class)`,
`@Config(sdk = [34])` and a `composeTestRule` inline. That is deliberate here:
extracting a shared base rule for a single test class is the premature
abstraction `.claude/rules/principles.md` warns against — there is no second
call site yet to prove the right shape for it. It becomes worth it around the
third or fourth screen; whoever adds that one should extract a shared JUnit
rule or base class instead of copying this boilerplate again.
