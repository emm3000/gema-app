# Release

How a Gema release is versioned, signed, minified and shipped to the Play
Console internal testing track.

## Version code and name

Both live in `gradle/libs.versions.toml`:

```toml
[versions]
gemaVersionCode = "1"
gemaVersionName = "1.0"
```

The `gema.android.release` convention plugin reads them and writes them into
`defaultConfig`. No module declares a version of its own.

To bump a release:

1. Increase `gemaVersionCode` by one. Play Console rejects a bundle whose
   version code is not higher than the last one uploaded to the track.
2. Set `gemaVersionName` to the human-facing version (`1.1`, `1.2.3`).
3. Commit both in the same change as the release notes.

## Upload keystore

The keystore is never generated into the repository and never committed.
Create it once, keep it outside the repo, and back it up: losing it means the
app can no longer be updated on Play under the same key unless Play App
Signing key rotation is used.

### 1. Create the keystore

Run this outside the repository (for example in `~/keys`):

```bash
mkdir -p ~/keys
keytool -genkeypair -v \
  -keystore ~/keys/gema-upload.jks \
  -alias gema-upload \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -storetype JKS
```

`keytool` asks for a store password, a key password and the certificate
identity (name, organizational unit, organization, city, state, country
code). Use `PE` as the country code. Store both passwords in a password
manager.

### 2. Point the build at it locally

Create `keystore.properties` at the repository root (git-ignored):

```properties
storeFile=/Users/<you>/keys/gema-upload.jks
storePassword=<store password>
keyAlias=gema-upload
keyPassword=<key password>
```

A relative `storeFile` resolves against the repository root; an absolute path
is used as given.

The plugin also accepts environment variables, which is what CI uses. They
are read only when the matching property is absent:

| Property | Environment variable |
|---|---|
| `storeFile` | `GEMA_STORE_FILE` |
| `storePassword` | `GEMA_STORE_PASSWORD` |
| `keyAlias` | `GEMA_KEY_ALIAS` |
| `keyPassword` | `GEMA_KEY_PASSWORD` |

When any of the four is missing or blank, no signing config is created and
the release build stays unsigned. A clean clone with no keystore still builds
debug, runs the tests and produces an unsigned `assembleRelease`.

### 3. Store it in GitHub

The `Release` workflow signs the bundle when these repository secrets exist:

| Secret | Value |
|---|---|
| `GEMA_UPLOAD_KEYSTORE_BASE64` | `base64 -i ~/keys/gema-upload.jks` output, one line |
| `GEMA_STORE_PASSWORD` | store password |
| `GEMA_KEY_ALIAS` | `gema-upload` |
| `GEMA_KEY_PASSWORD` | key password |
| `PLAY_SERVICE_ACCOUNT_JSON` | Play Console service account JSON, needed only for publishing |

On macOS, `base64 -i file | pbcopy` copies the value without a trailing
newline.

Publishing is off until the repository variable `PLAY_PUBLISH_ENABLED` is set
to `true`.

## Building

```bash
./gradlew assembleRelease   # APK, for local install and smoke checks
./gradlew bundleRelease     # AAB, what Play Console consumes
```

Outputs land in `app/build/outputs/apk/release/` and
`app/build/outputs/bundle/release/`. An unsigned build is named
`app-release-unsigned.apk`; a signed one is `app-release.apk`.

## Minification

The release build runs R8 with `proguard-android-optimize.txt` plus
`app/proguard-rules.pro`, and shrinks resources.

`proguard-rules.pro` only keeps `SourceFile` and `LineNumberTable` so Play
Console crash reports stay readable, and renames the source file attribute so
the original file names are not shipped. Upload
`app/build/outputs/mapping/release/mapping.txt` to Play Console with every
release, otherwise stack traces arrive obfuscated.

Koin, SQLDelight and Compose need no rules of their own here. Koin resolves
through lambdas rather than reflection, SQLDelight generates plain Kotlin,
and Compose ships its own consumer rules. R8 reports no missing rules
(`app/build/outputs/mapping/release/missing_rules.txt` is not produced). The
SIAGIE import and export use `javax.xml`, which is part of the Android
platform and therefore out of R8's reach.

If a future dependency does need a rule, R8 writes the exact rule it wants to
`missing_rules.txt`; copy it into `proguard-rules.pro` rather than adding a
broad `-keep`.

### Smoke check

The minified build must be run before it is shipped. What was verified for
this change, on an API 37 emulator with a throwaway keystore created outside
the repository:

1. `./gradlew assembleRelease bundleRelease` with the four environment
   variables set produced a signed `app-release.apk` and `app-release.aab`.
2. `adb install -r app-release.apk`, then launch: the setup flow rendered its
   first step with the computed school-year periods, which exercises Koin
   injection, Compose and the domain layer.
3. Advancing to step two rendered the section step, which exercises the
   SQLDelight read and write path.
4. `adb logcat` showed no `FATAL`, no `ClassNotFoundException` and no
   `NoSuchMethodError`.

The SIAGIE import and export and the backup restore are not covered by that
walkthrough; run them manually on a minified build before the first public
release.

## Shipping to internal testing

1. Push to `main`. The `Release` workflow builds the bundle and uploads it as
   the `gema-release-bundle` artifact.
2. While `PLAY_PUBLISH_ENABLED` is unset, download that artifact and upload
   the `.aab` by hand in Play Console under
   **Test and release > Testing > Internal testing > Create new release**.
3. Once the Play Console app exists and the service account is wired, set
   `PLAY_PUBLISH_ENABLED` to `true` and run the `Release` workflow from the
   Actions tab, choosing the track. The release is created as a draft, so
   rollout stays a manual decision.
