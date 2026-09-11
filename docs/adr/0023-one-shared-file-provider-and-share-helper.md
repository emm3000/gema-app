---
status: accepted
---
# One `FileProvider` and one share helper live in `core:ui`

`feature:backup`, `feature:export` and `feature:attendance` each shipped
their own `FileProvider` declaration, their own `file_paths` XML resource
and their own copy of the same four lines that turn a `File` into a
`content://` URI and launch an `ACTION_SEND` chooser. Three manifests, three
authorities (`.backups`, `.exports`, `.attendance`) and three near-identical
Kotlin files did the same job.

`core:ui` now owns the single `FileProvider` declaration, at authority
`${applicationId}.shared_files`, backed by one `gema_shared_file_paths.xml`
listing every cache subdirectory a feature writes shareable files into
(`backups/`, `exports/`, `attendance-exports/`). `core:ui/share/ShareFile.kt`
exposes `Context.shareFile(file, mimeType, chooserTitle)`, the single call
every feature route uses instead of building its own intent.

## Why `core:ui`

Every feature module already depends on `core:ui` (`CLAUDE.md`'s module
graph), and it is already an Android library module with its own manifest
merge into `app`. No feature module may depend on another feature module, so
the provider could not live in `feature:backup`, `feature:export` or
`feature:attendance` without the other two reaching into a sibling feature.
`core:domain` is JVM-only and cannot hold a `FileProvider`. `core:ui` is the
only module that is both Android and shared by all three, so it needs no new
Gradle module and no boundary exception.

## Considered Options

- **Keep one `FileProvider` per feature.** Rejected: three declarations,
  three XML resources and three copies of the same intent-building code for
  behavior that is identical except for a directory name.
- **A fourth `core:share` module.** Rejected: nothing else would depend on
  it, and `core:ui` already reaches every feature; a module that exists to
  hold one file is YAGNI.
- **Put the helper in `app`.** Rejected: `app` cannot be reached by a feature
  module (`feature:* -> core:domain, core:ui` only), so a feature route could
  never call it.

## Consequences

- One authority, one `file_paths` XML, one manifest `<provider>` entry to
  keep in sync when a new feature needs to share a file — extend
  `gema_shared_file_paths.xml` with a `cache-path`, not a new provider.
- `buildShareIntentSpec` is a pure function (no `Context`, no `FileProvider`)
  and is unit-tested in `core:ui`; `Context.shareFile` itself calls
  `FileProvider.getUriForFile` and `startActivity`, which need an Android
  runtime and stay untested at the unit level, the same tradeoff
  `BackupContextActions.kt` (now `RestartOutcome.kt`) already made for
  `Context.restart()`.
- `feature:backup`, `feature:export` and `feature:attendance` no longer
  declare a manifest, a `FileProvider` subclass or a `file_paths` resource of
  their own.
