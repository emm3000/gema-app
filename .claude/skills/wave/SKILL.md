---
name: wave
description: "Trigger: /wave, levantar wave, abrir peers, dispatch wave, lanzar sesiones. Boot one Warp pane per ticket and dispatch each ticket to its peer session."
argument-hint: <issue numbers>
allowed-tools: Bash(gh:*) Bash(scripts/gema-wave:*) Bash(git worktree:*) ListAgents SendMessage Read
disable-model-invocation: true
license: Apache-2.0
metadata:
  author: "emm3000"
  version: "1.0"
---

## Activation Contract

Run when the owner invokes `/wave` with one or more issue numbers, or when the orchestrator starts the next wave itself after the previous wave is fully merged. Each number becomes one peer session and one dispatch. Stop and report if any number is not an open `ready-for-agent` issue.

## Hard Rules

- Read `docs/agents/multi-session.md` first. Its dispatch checklist, model table and isolation rules bind every dispatch.
- Never queue two tickets on one peer. Never dispatch a ticket while a PR from the same wave is unmerged.
- One explicit model and one explicit effort per ticket, stated to the owner before booting.
- Never touch the owner's main checkout. Peers get their own worktree from `scripts/gema-session`.

## Decision Gates

| Ticket kind | Model:effort | Extra instruction |
|---|---|---|
| Layout or restyle only | sonnet:medium | visual check |
| Adds or changes behavior | sonnet:medium | load `mattpocock-skills:tdd`, red-green-refactor |
| Migration, xlsx bytes, cross-module | opus:high | visual check if a screen changes |

## Execution Steps

1. For each issue run `gh issue view <n> --json title,labels,body`. Confirm the label and derive a short lowercase pane name from the title (one word, no digits).
2. Classify each ticket with the table. The table binds: deviate only with a one-line reason stated in the plan, never silently. Tell the owner the plan in one line per ticket: `@<name> #<n> <model>:<effort>`, before booting anything.
3. Run `scripts/gema-wave <name>:<model>:<effort> ...` once with every ticket.
4. Poll `ListAgents` until every pane name is listed, at most 60 seconds.
5. Send each peer one dispatch built from the playbook checklist: issue, docs to read, branch `<type>/<n>-<slug>`, its worktree `../gema-<name>`, the acceptance-criteria line, gates, TDD or visual check per the table, `Closes #<n>`, no merge, reply with the PR URL. Ask for `notify_when_idle`.
6. Report to the owner in one or two lines: peers booted, tickets dispatched.

## Output Contract

Return the list `@<name> #<n> <model>:<effort>` and nothing else until a peer reports back.

## References

- `docs/agents/multi-session.md` — dispatch checklist, isolation, review cycle.
- `scripts/gema-wave` — Warp tab config generator.
- `scripts/gema-session` — worktree plus `claude` launcher.
