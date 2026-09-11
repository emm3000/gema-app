---
status: accepted
---
# Release signing, versioning and minification live in a convention plugin

ADR 0008 put shared Gradle configuration in convention plugins and left module
build files with nothing but their plugin and their dependencies. Release
configuration was the last thing breaking that rule: `app/build.gradle.kts`
read `keystore.properties`, built a signing config, hardcoded `versionCode`
and `versionName`, and wired the release build type. All of it now lives in
`gema.android.release`, which `:app` applies instead of
`gema.android.application`.

## The keystore is optional, and absence is not a special case

The plugin reads four credentials — store file, store password, key alias, key
password — from `keystore.properties` at the repository root, falling back per
credential to `GEMA_STORE_FILE`, `GEMA_STORE_PASSWORD`, `GEMA_KEY_ALIAS` and
`GEMA_KEY_PASSWORD`. When any of the four is missing or blank, no signing
config is created at all and the release build type is signed with nothing.

The previous code created the config conditionally and then looked it up again
with `signingConfigs.findByName("config")`, which silently returns `null` for
two different reasons: the keystore is absent, or the name was mistyped. The
plugin keeps one boolean, decides once, and creates and reads the config under
that same decision, so a typo is a build failure rather than an unsigned APK.

A clean clone with no keystore builds debug, runs the tests and produces an
unsigned release. That is the property CI depends on: `assembleRelease` runs on
every pull request, including from a fork, where no secret is readable.

`keystore.properties` is read through `providers.fileContents`, not through
`File.readText`. The configuration cache tracks a provider and does not track a
raw file read, so with the plain read the first cached configuration would
survive the owner creating the keystore file and keep producing unsigned
releases until something else invalidated the cache.

## One version, in the version catalog

`gemaVersionCode` and `gemaVersionName` are `[versions]` entries in
`gradle/libs.versions.toml`, which `CLAUDE.md` already names as the only place
a version is written. The plugin reads them through `VersionCatalogsExtension`.
A release bump is a two-line edit in the file the project already treats as the
single source of versions, documented in `docs/release.md`.

## R8 rules are added on evidence, not on reputation

The release build minifies and shrinks resources. `proguard-rules.pro` keeps
`SourceFile` and `LineNumberTable` for readable Play Console crash reports and
renames the source file attribute; it keeps nothing else.

Koin, SQLDelight and Compose are commonly given blanket `-keep` rules. None are
added here: Koin resolves through lambdas rather than reflection, SQLDelight
generates ordinary Kotlin, Compose ships consumer rules, and the SIAGIE
workbook code reaches `javax.xml`, which lives in the platform and is not
shrunk. R8 produced no `missing_rules.txt`, and a minified, signed build
installed on an API 37 emulator ran every flow that touches `java.util.zip`,
the DOM parser or a file provider: SIAGIE import, SIAGIE grades export,
monthly attendance export, and backup with restore, plus the setup steps that
exercise Koin, Compose and the SQLDelight read and write path. Logcat showed
no `FATAL EXCEPTION`, `ClassNotFoundException` or `NoSuchMethodError`.
`docs/release.md` records that walkthrough so the next release repeats it. A
`-keep` added without that evidence only hides the breakage it is meant to
prevent and silently grows the APK.

## The launcher icon is adaptive only

`minSdk` is 26, the first level with adaptive icons, so every device this app
installs on resolves `mipmap-anydpi-v26`. The density-bucket `ic_launcher.webp`
files could never be reached and were deleted rather than regenerated from the
brand assets.

`android:roundIcon` went with them. It exists for pre-26 launchers that wanted
a circular raster; an adaptive icon already supplies every mask the launcher
asks for, including the round one, from the same foreground and background
layers. Keeping a second entry point would mean maintaining two icons that can
drift apart while only one of them is ever used.

## Publishing stays outside the build

The `Release` workflow builds the bundle and publishes it with
`r0adkll/upload-google-play`, not with the Gradle Play Publisher plugin. Play
publication is a CI concern: Gradle Play Publisher would add a plugin, a
version catalog entry and a service account file to every local build's
configuration for a task only CI ever runs, while the action keeps that surface
in the workflow where the credentials already live.

The publish job is gated on the `PLAY_PUBLISH_ENABLED` repository variable and
on a manual dispatch, and creates the release as a draft. Until the Play
Console app exists, the bundle is downloaded from the workflow artifact and
uploaded by hand.
