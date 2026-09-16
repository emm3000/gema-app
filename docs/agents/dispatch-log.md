# Dispatch log

One row per merged PR that a peer session implemented. The orchestrator appends it at cycle close. The table in `.claude/skills/wave/SKILL.md` is tuned from this log: a row with two or more FIX FIRST verdicts whose cause is `judgment` moves one step up; a row that stays MERGE across many PRs may move one step down.

Cause values: `checklist` (a recurring item from the screen dispatch checklist, the model was fine), `judgment` (a wrong decision the model made), `spec` (the issue was wrong or thin).

| PR | Issue | Row | Model:effort | First review | Cause |
|---|---|---|---|---|---|
| #274 | #265 | 3 | sonnet:medium | MERGE | minor doc nit fixed before merge |
| #275 | #255 | 4 | sonnet:medium | FIX FIRST | checklist: 3 glyph-banner screenshots missing |
