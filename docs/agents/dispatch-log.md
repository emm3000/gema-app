# Dispatch log

The table in `.claude/skills/wave/SKILL.md` is tuned from this log. A table row with two or more `judgment` FIX FIRST verdicts moves one step up. A row that stays MERGE across many PRs may move one step down.

The log has a fixed size. The Summary keeps the totals per table row forever. Recent keeps only the last 20 PRs. At cycle close the orchestrator appends the new PR to Recent. When Recent passes 20 rows, it adds the oldest rows to the Summary counts and deletes them.

Cause values: `checklist` (a recurring item from the screen dispatch checklist, the model was fine), `judgment` (a wrong decision the model made), `spec` (the issue was wrong or thin).

## Summary

Totals of rows already folded out of Recent.

| Row | Model:effort | PRs | MERGE | FIX FIRST checklist | FIX FIRST judgment | FIX FIRST spec |
|---|---|---|---|---|---|---|
| 1 | sonnet:low | 0 | 0 | 0 | 0 | 0 |
| 2 | sonnet:medium | 0 | 0 | 0 | 0 | 0 |
| 3 | sonnet:medium | 0 | 0 | 0 | 0 | 0 |
| 4 | opus:medium | 0 | 0 | 0 | 0 | 0 |
| 5 | opus:high | 0 | 0 | 0 | 0 | 0 |

## Recent

Last 20 PRs, oldest first. Model:effort is what actually ran, which may differ from the table row.

| PR | Issue | Row | Model:effort | First review | Cause |
|---|---|---|---|---|---|
| #274 | #265 | 3 | sonnet:medium | MERGE | minor doc nit fixed before merge |
| #275 | #255 | 4 | sonnet:medium | FIX FIRST | checklist: 3 glyph-banner screenshots missing |
| #279 | #254 | 1 | sonnet:low | MERGE | |
| #280 | #268 | 3 | sonnet:medium | MERGE | |
| #281 | #266 | 4 | sonnet:medium | MERGE | ran below table row (additive `GButton` parameter, one screen) |
| #285 | #267 | 1 | sonnet:low | MERGE | |
| #286 | #271 | 4 | sonnet:medium | FIX FIRST | checklist: TalkBack screenshot focused the wrong node; ran below table row |
| #288 | #270 | 1 | sonnet:low | MERGE | |
| #289 | #269 | 4 | opus:medium | MERGE | |
| #290 | #283 | 4 | opus:medium | MERGE | |
