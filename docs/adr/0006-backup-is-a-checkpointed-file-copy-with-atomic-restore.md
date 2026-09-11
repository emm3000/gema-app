---
status: accepted
---
# Backup is a checkpointed file copy; restore is atomic via a sibling file

A backup must capture every row of the current session, and a restore must
never leave the app with a half-written database if it is interrupted.
`SqliteBackupStore` runs `PRAGMA wal_checkpoint(TRUNCATE)` on the live
`SqlDriver` before copying `gema.db`, so the write-ahead log is folded into the
main file first and the copy is never missing recent writes. Restore never
writes into `gema.db` directly: it streams the picked document into a sibling
`gema.db.incoming` file, then moves that file over `gema.db` with
`StandardCopyOption.ATOMIC_MOVE`, and only then deletes the old `-wal` / `-shm`
sidecars. `RestoreBackupUseCase` validates the SQLite header and schema
version before the stream even starts.

## Considered Options

- **Copy the live file without a checkpoint.** Rejected: a backup could miss
  rows still sitting in the WAL, silently losing the current session.
- **Stream the restored bytes directly into `gema.db`.** Rejected: a crash or
  an interrupted `ACTION_OPEN_DOCUMENT` stream mid-write would leave the
  active database corrupt with no way back.

## Consequences

- `SqliteBackupStoreTest` proves the round trip against real files on disk,
  including that an interrupted restore leaves the current database
  untouched.
- No storage permission and no network: backup goes through the share sheet
  via a `FileProvider` from the app's cache directory, restore comes in
  through `ACTION_OPEN_DOCUMENT`.
- The reminder threshold and the last-backup timestamp live in shared
  preferences, not the database, because a restore replaces the database.
