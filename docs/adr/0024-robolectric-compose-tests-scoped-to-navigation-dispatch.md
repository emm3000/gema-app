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

## Verification

`HomeScreenTest` was confirmed to catch the exact #136 regression: with
`SectionCard`'s `onClick` removed, the test failed because no intent was
captured; with `onClick` restored, it passed.
