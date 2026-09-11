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
  -keystore ~/keys/gema-upload.keystore \
  -alias gema-upload \
  -keyalg RSA -keysize 4096 -validity 10000
```

`keytool` asks for a store password, a key password and the certificate
identity (name, organizational unit, organization, city, state, country
code). Use `PE` as the country code. Store both passwords in a password
manager.

The build reads whatever format the file already is. Modern `keytool`
produces PKCS12, older keystores are JKS, and both work with no extra
setting, because the JVM resolves the type from the file itself. An existing
`.p12` or `.jks` upload keystore is used as it is — only the path in
`storeFile` has to point at it.

### 2. Point the build at it locally

Create `keystore.properties` at the repository root (git-ignored):

```properties
storeFile=/Users/<you>/keys/gema-upload.keystore
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
debug, runs the tests and produces an unsigned `assembleRelease`. When some
but not all four are present, the build warns and names the missing ones
before falling back to unsigned, so a typo in `keystore.properties` or a
secret that never reached the runner is visible in the build log instead of
producing a silently unsigned release.

### 3. Store it in GitHub

The `Release` workflow signs the bundle when these repository secrets exist:

| Secret | Value |
|---|---|
| `GEMA_UPLOAD_KEYSTORE_BASE64` | `base64 -i ~/keys/gema-upload.keystore` output, one line |
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

The minified build must be run before it is shipped, and the walkthrough has
to reach every flow that touches `java.util.zip`, the DOM parser or a file
provider, because those are the ones R8 can break without any build-time
warning.

What was verified for this change, on an API 37 emulator, with a throwaway
keystore created outside the repository and the fixtures in
`core/siagie/src/test/resources` pushed to `/sdcard/Download`:

1. `assembleRelease` and `bundleRelease` with the four environment variables
   set produced a signed `app-release.apk` and `app-release.aab`. The
   throwaway keystore was PKCS12, and `apksigner verify --print-certs`
   reported its certificate on the packaged APK.
2. Setup: both steps rendered and saved, which exercises Koin injection,
   Compose, the domain layer and the SQLDelight write path.
3. SIAGIE import: picking `6 Primaria EBR.xlsx` for a 1° section was rejected
   with "Este archivo no es de esta sección" after the file's own 6° grade was
   read, and picking it for a 6° A section previewed "5 alumnos en el archivo"
   and created the five students.
4. SIAGIE grades export: "Generar archivo" rewrote the imported workbook and
   opened the share sheet with it.
5. Monthly attendance: a day was recorded (four present, one absent), the
   monthly summary counted it per student, and "Exportar el mes" filled
   `AsistenciaIE_12345_6_A.xlsx` and shared it.
6. Backup and restore: "Crear respaldo" wrote a `.gema` file and shared it,
   and restoring a `.gema` from a different school year replaced the data and
   restarted the app on the restored year.
7. `adb logcat` showed no `FATAL EXCEPTION`, no `ClassNotFoundException` and
   no `NoSuchMethodError` at any point.

No keep rule was needed for any of it.

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
