---
status: accepted
---
# Rewrite the domain model and database schema instead of evolving them

The previous Gema code modelled a generic gradebook with numeric grades,
evaluations and a remote API. The new domain (School Year, Periods, Areas,
Competencies, literal levels, SIAGIE exchange) does not map onto it. We start
the domain and SQLDelight schema from scratch under the module layout defined
in CLAUDE.md, keeping only the stack, tooling and lessons learned. There is no
migration path from the old schema because the app was never released.
