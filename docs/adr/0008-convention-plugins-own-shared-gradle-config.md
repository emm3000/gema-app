---
status: accepted
---
# Convention plugins are the only place SDK levels and shared Gradle config live

`build-logic` is an included build providing `gema.android.application`,
`gema.android.library`, `gema.android.compose`, `gema.android.feature` and
`gema.jvm.library`. Each one sets `compileSdk`, `minSdk`, `targetSdk`, the
Java 17 toolchain and (where relevant) Compose, once. A module's own
`build.gradle.kts` applies exactly one of these plugins and declares its own
dependencies — nothing else. `gradle/libs.versions.toml` is the only place a
dependency version is written. A root-level `checkModuleBoundaries` task
walks every module's declared dependencies and fails the build when a feature
module is reached by anything but `app`, when a core module reaches a core
module other than `core:domain`, or when `core:domain` / `core:siagie` apply
an Android plugin.

## Considered Options

- **Repeat `compileSdk` / `minSdk` / Java version in every module's build
  file.** Rejected: nineteen modules times one Gradle upgrade is nineteen
  edits, and the version drifts the moment one is missed.
- **Enforce module boundaries by convention only (code review), with no
  build-time gate.** Rejected: a stray `implementation(project(":feature:x"))`
  from another feature module compiles silently and is easy to miss in
  review.

## Consequences

- Adding a module means applying one convention plugin, not copying a build
  file and editing the parts that should have stayed shared.
- `checkModuleBoundaries` runs alongside `detekt` and `testDebugUnitTest` as a
  required gate; a boundary violation fails CI with a named module and a
  named forbidden dependency rather than surfacing later as a runtime cycle.
- An AGP upgrade that changes the extension API (for example AGP 9's default
  DSL) is fixed in the convention plugins, once, rather than in every
  `core:*` and `feature:*` build file.
