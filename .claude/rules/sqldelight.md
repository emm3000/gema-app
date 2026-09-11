---
paths:
  - "core/database/src/main/sqldelight/**"
  - "core/database/src/test/kotlin/com/emm/gema/core/database/migration/**"
---

# SQLDelight schema rules

`GemaDb` is the source of truth. Every schema change is a migration unit with its own falsifier.

## The snapshot is part of the diff

- A change to any `.sq` that alters a `CREATE` statement ships three artifacts in the same commit: the `.sq` edit, `com/emm/gema/core/database/N.sqm` where `N` is the version before the bump, and `databases/(N+1).db`.
- Generate the snapshot with `./gradlew :core:database:generateDebugGemaDbSchema`. It writes the current version to `databases/`.
- `./gradlew verifySqlDelightMigration` is the floor, not the proof. It replays every existing snapshot forward but cannot notice a snapshot that was never written. `checkSqlDelightSnapshots` runs before it and fails on any `N.sqm` without its `(N+1).db`.
- Never delete or regenerate a committed `.db`. Each one is the exact schema a shipped build wrote to disk.

## Writing the migration

- Additive only: `ALTER TABLE ... ADD COLUMN` appended last, nullable or with a default. Renames and drops go through a new table plus a copy.
- **The new column goes at the END of `CREATE TABLE` too.** `ADD COLUMN` appends, so a column placed mid-table next to its logical neighbours leaves a fresh install and a migrated install disagreeing on column order. The failure is `verifySqlDelightMigration` reporting an `ordinalPosition - CHANGED` mismatch — that message means the position, not the type.
- Every `N.sqm` has a unit test under `core/database/src/test/kotlin/com/emm/gema/core/database/migration/` that builds schema `N` by hand, inserts legacy rows, migrates to `GemaDb.Schema.version` and asserts the rows survive.
- Migration tests never use `GemaDb.Schema.create`, because that creates the latest schema and skips the migration under test.
- Prove the migration test is not vacuous before trusting it: set its `oldVersion` constant to the current schema version, so the migration is skipped, and watch both cases fail on the missing column. Restore it afterwards. A migration test that passes without running the migration proves nothing.
