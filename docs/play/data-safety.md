# Play Console — Data safety form (`docs/play/data-safety.md`)

Source for **App content > Data safety** in Play Console. Gema has no
network access and no backend, so almost every question is answered "no" or
"not applicable"; each answer states why.

## Does your app collect or share any of the required user data types?

**Answer: No.**

Justification: Gema has no internet permission, no backend, no analytics
SDK, no crash-reporting SDK, and no advertising SDK. Nothing the app does
leaves the device, so there is no data to declare as collected or shared.

Because the top-level answer is "No", every data-type category below (personal
info, financial info, health and fitness, messages, photos and videos, audio
files, files and docs, calendar, contacts, app activity, web browsing, app
info and performance, device or other IDs) is left unchecked in the Play
Console form. The table below documents why for the categories a reviewer is
most likely to ask about, given the app's feature set.

| Data type | Collected? | Shared? | Justification |
|---|---|---|---|
| Name (student names, entered/imported by the teacher) | No | No | Stored only in the local SQLite database on the device; never transmitted anywhere. |
| Other personal info (student code, SIAGIE id) | No | No | Same as above: local-only, entered or imported by the teacher, never sent off-device. |
| Files and docs (imported/exported SIAGIE `.xlsx`, `.gema` backup) | No | No | Files are read from and written to device storage the teacher explicitly picks; Gema does not upload them or read them for any purpose beyond import/export. |
| App activity, app info and performance | No | No | No analytics, telemetry, or crash-reporting SDK is included. |
| Device or other identifiers | No | No | No advertising ID, install ID, or hardware identifier is read or transmitted. |

## Is all of the user data collected by your app encrypted in transit?

**Answer: Not applicable — no data is transmitted.**

Justification: Gema has no network permission and performs no network
requests, so there is no transit to encrypt.

## Do you provide a way for users to request that their data is deleted?

**Answer: Yes, data is deleted automatically when the app is uninstalled or
its storage is cleared; no separate request mechanism is needed.**

Justification: All data lives in the app's private local database. Standard
Android uninstall (or "Clear storage") removes it completely. Backups are
files the teacher creates and controls outside the app; the teacher deletes
those the same way they delete any file on their device.

## Has your app committed to follow the Play Families Policy?

**Answer: No.**

Justification: Gema targets adult teachers and is not designed for or
directed at children (see `docs/play/target-audience.md`).

## Has your app been independently validated against a global security standard?

**Answer: No.**

Justification: No independent security certification has been sought; this
is a small offline utility app with no network attack surface to certify.

## Data safety summary to paste into the form's free-text notes

```
Gema funciona sin conexión a internet. No tiene backend, no usa SDKs de
terceros para analítica, publicidad o rastreo, y no recolecta ni comparte
datos con nadie. Toda la información (años escolares, secciones,
estudiantes, asistencia, niveles de logro) se guarda solo en la base de
datos local del dispositivo. Los respaldos son archivos que el propio
docente genera y comparte por su propia decisión; Gema no los sube a
ningún servicio. Desinstalar la app elimina todos los datos guardados.
```
