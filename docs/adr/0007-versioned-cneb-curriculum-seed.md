---
status: accepted
---
# The CNEB curriculum seed is versioned and idempotent

`competency` carries a `curriculum_version` column and is populated with
`INSERT OR REPLACE` on the competency id, inside one transaction
(`SqlDelightCompetencyRepository.seed`). `PrimaryCurriculum` is a JVM constant
in `core:domain` holding the 28 area competencies of
`docs/cneb/primary.json`, keyed by `Area`, behind `PrimaryCurriculum.VERSION`
(currently `1`). A reinstall or an app upgrade re-runs the seed safely because
`INSERT OR REPLACE` never duplicates a row, and a future curriculum change is
an additive migration plus a bumped `VERSION`, not a destructive rewrite of
the table.

## Considered Options

- **Delete-then-insert on every seed.** Rejected: it would need to reconcile
  `worked_competency` rows pointing at the deleted ids on every app start.
- **Seed once at install and never touch the table again.** Rejected: it
  gives no path to ship a later curriculum version (for example adding the
  transversal CNEB competencies) without a bespoke migration for existing
  installs.

## Consequences

- The 2026 transversal competencies (TIC, "Gestiona su aprendizaje") are
  deliberately excluded from version 1: they belong to no `Area`, so adding
  them is its own curriculum version and its own decision about whether they
  hang off an `Area` or a new owner type.
- Tests cover seed contents, per-area ordinal contiguity, and seeding
  idempotence at both the use-case and the SQLite level.
- `ARTE`'s real SIAGIE sheet tab is unconfirmed against a live export
  (`docs/cneb/README.md`); the seed may need a correction inside version 1
  before a version 2 is warranted.
