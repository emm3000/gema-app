# Multi-session orchestration

How the owner runs several Claude Code sessions on this repo in parallel, and what the orchestrator session must do before dispatching work to them. Read this before dispatching a ticket to a peer session.

## Roles

- **Owner** approves the first wave of a session and verifies model and effort with `/model` in each pane. Once a wave is fully merged the orchestrator starts the next one on its own. Peer sessions are booted only through the `/wave` skill (see Launching peers), never by running `scripts/gema-wave` by hand. Refer to a session with an `@` prefix in chat (`@tokens`) — it disambiguates the session from a feature or ticket of the same name.
- **Orchestrator** coordinates: dispatches via `SendMessage`, reviews, merges. It stays thin.
  - Delegate investigation and any artifact-producing work (tickets, specs, surveys, docs) to a subagent with an explicit model.
  - Do inline only routing state (`git status`, `git worktree list`, `gh issue/pr list`, `ListAgents`) and at most 1-2 files to decide. *Why: the owner stopped the orchestrator grepping `build-logic` to write a ticket itself (2026-09-12).*
  - Report minimal: act on review/agent findings, tell the owner 1-2 lines and only decisions that are genuinely theirs. Merging a clean PR is normal practice, not a question. *Why: the owner flagged relayed review output as noise.*

## Model and effort

- Every dispatch states model and **one** explicit effort (low / medium / high, never a range), with a one-line reason. Pick the cheapest model and lowest effort that gets it right; reserve Opus/high for work where a mistake is silent or expensive (xlsx byte layout, migrations); low is enough where a wrong answer fails tests loudly.
- Fable (`claude-fable-5-1`) is for architecture and design decisions only — identity, tokens, component rules, mockups, visual judgment. Reviews, implementation and doc checks go to Opus or Sonnet. *Why: owner correction when Fable was proposed to review the docs PR #171.*
- A session cannot see its own reasoning effort — `ListAgents` doesn't show it or context usage, and asking a session directly returns an unreliable guess. The owner verifies effort with `/model` in each terminal. *Why: on 2026-09-12 sessions reported "low" while one said the level isn't exposed to it.*
- The orchestrator can't self-manage its own effort either: tell the owner when to raise it (a conflicting rebase, judging byte-level findings) and when to lower it back.

## Slicing and waves

- One slice = one small PR: a migration + domain change, or one screen, or one integration. A ticket naming more than 2 screens, or a migration plus a screen, gets split into sub-issues with `gh` first.
- If a session passes ~60% context without a PR, it commits, opens a partial PR, clears, and continues. *Why: a session hit 50% context on a ticket that bundled four tickets into one.*
- Waves are ordered by dependency; parallelism is safe only within a wave (e.g. tokens -> typeface -> component slices in parallel -> screens in parallel). A wave starts only after the previous one is merged.
- Before dispatching tickets filed by an audit, re-verify each against current `main` — the finding may already be fixed. *Why: the visual audit #117-#124 ran on an older commit, and part of #120 was already fixed by #116.*

## Dispatch prompt checklist

Every dispatch to a peer session must include:

- Issue number, docs to read first, branch name, and the peer's **own** worktree path (see Isolation below).
- The line: *"The issue's acceptance criteria are the contract and win over any file list here; run every criterion check before opening the PR."* Prefer criteria phrased as a command with expected empty output. *Why: PRs #84 and #85 stopped at the file list and missed matching call sites.*
- Gates: `./gradlew detekt testDebugUnitTest checkModuleBoundaries` green before commit; conventional commits; no `Co-Authored-By`.
- For any ticket that adds or changes behavior (a use case, a ViewModel rule, a UiState field that is derived rather than laid out), an instruction to load the `mattpocock-skills:tdd` skill and work red, green, refactor, writing the failing behavior test before the code. Pure layout or restyle tickets skip it. *Why: #168 and #170 added behavior without test-first, and the PR #183 review found an untested path, a saved C with a blank conclusion (2026-09-13).*
- For any screen-touching ticket, a visual check: install on the session's assigned emulator only, screenshot every changed screen with `adb -s <serial> exec-out screencap -p`, and compare against `docs/design/screens.md`. Then publish the screenshots on a branch named `assets/<N>-visual-check`, with the PR head short SHA in every file name (for example `home-<sha>.png`), and link them in the PR body with `raw.githubusercontent.com` URLs, as PR #152 did. The `gh` CLI and the API cannot attach images to a PR. The assets branch is deleted when the cycle closes. *Why: `SetupYear` drifted from the approved design in #4 and a code-only review never caught it. PRs #173 and #174 then shipped without images, which forced the reviewer to boot its own emulator.*
- An instruction to keep the slice small and stop and report instead of expanding scope.
- An instruction to open the PR with `Closes #N`, not merge it, not watch CI, and message the orchestrator the PR URL in 1-2 lines.

## Launching peers

- `/wave <issue numbers>` is the only entry point, whether the owner types it or the orchestrator invokes it for the next wave: it classifies each ticket with the skill table, states `@<name> #<n> <model>:<effort>` to the owner before booting (deviations from the table carry a one-line reason), runs `scripts/gema-wave`, waits for the peers in `ListAgents` and dispatches. The skill lives in `.claude/skills/wave/SKILL.md`.
- `scripts/gema-wave name:model:effort [...]` writes `~/.warp/tab_configs/gema-wave.toml` with one pane per peer in a horizontal split and opens it with `open "warp://tab_config/gema-wave"`. A Tab Config opens as a new tab in the active Warp window. Launch Configurations (`warp://launch/`) always open a new window, so they are not used.
- `scripts/gema-session name model effort` creates the detached worktree `../gema-<name>` from `origin/main` when missing, then runs `claude -n <name> --model <model> --effort <effort> --permission-mode bypassPermissions` inside it. The peer creates its ticket branch with `git switch -c`.
- Closing a wave: the owner closes the panes; the orchestrator removes the worktrees. To close a pane from a script, kill the `claude` pid and then `kill -HUP` its parent `zsh`, whose parent is the Warp process.
- *Why: until 2026-09-16 the owner opened every terminal by hand and set name, model, effort and permission mode in four steps per peer.*

## Isolation: worktrees

- Every peer works in its own git worktree (`/Users/emm/AndroidStudioProjects/gema-<name>`, branched off `origin/main`) — never in the owner's main checkout, which holds owner-only uncommitted files. All sessions open in the same folder by default, so a checkout there changes the branch under every other session. *Why: `gema-polish` implemented #157 in the main checkout, and the orchestrator, assuming it was a leftover, switched the checkout back to `main` mid-fix (2026-09-12).*
- Before changing the state of any checkout, find out who is using it — an unexpected branch may be a live peer, not a leftover.
- Review and verification prompts are read-only on every existing checkout. If gradle must run on a branch, or a red/green check needs a source edit, use a throwaway worktree under the session scratchpad and remove it afterward.
- `git checkout main` fails inside a worktree while the primary worktree is already on `main`; use `git fetch` + `git switch -c <branch> origin/main` (or `checkout -B`) instead.
- Cleanup is part of closing the cycle, not a later chore (see Between tickets). *Why: on 2026-09-12 five worktrees from merged PRs #150, #151, #156 and #159, plus a review scratch worktree, were still on disk a wave later, and one of them blocked the name a new session needed.*

## Isolation: emulators

- Each session gets its own emulator serial. The owner does not use an emulator, so none is reserved. Never install on another session's emulator.
- `./gradlew installDebug` installs on **every** connected adb device. Run `adb devices` to confirm the serial, then use `ANDROID_SERIAL=<serial> ./gradlew installDebug` or `adb -s <serial> install -r <apk>`, and scope screenshots with `-s` too. *Why: `gema-polish`'s build overwrote the APK on another peer's emulator and invalidated that peer's visual check (2026-09-12).*
- If it happens anyway, the owning session reinstalls its own build — the session that caused the overwrite must not touch another session's device.
- A session boots its own emulator only when it needs one, with a fixed port so that the serial is predictable: `emulator -avd <avd> -port <port> -no-snapshot-save &` gives serial `emulator-<port>`.
- When the review cycle closes, shut the emulator down with `adb -s <serial> emu kill`, together with the worktree cleanup. Review subagents shut down the spare emulator they booted. Keep the AVD: a later boot reuses it, and deleting AVDs frees only disk, not RAM or CPU. Delete an AVD (`avdmanager delete avd -n <avd>`) only when no planned ticket uses it. Never shut down an emulator that another session still uses. *Why: on 2026-09-12 five emulators stayed running while only one session was working (owner request to save resources).*

## Review cycle

- A session gets nothing new until its previous PR is reviewed, fixed and merged. Never queue two tickets in one dispatch. *Why: owner directive — finish the review cycle before sending anything else.*
- Two-axis review (standards vs. `CLAUDE.md` rules, spec vs. the issue), plus screenshots checked against `screens.md`. Reviews of mechanical slices, such as component restyles, run Sonnet. Reviews of screens, migrations and logic run Opus high. Post-review fixes run Sonnet low.
- Every PR review is a fresh subagent that the orchestrator launches, one per PR. It is not a long-lived peer session. The subagent is read-only. It does not boot an emulator, build the app or rerun detekt and the tests, because required CI already runs those. It reads the diff and the issue, and it pulls the screenshots locally with `git show origin/assets/<N>-visual-check:<file>`. It checks that the SHA in each file name matches the PR head, and then views the images. It boots the spare emulator in a throwaway worktree only when screenshots are missing, stale or suspicious, and shuts it down afterward. It returns a short MERGE or FIX FIRST verdict. *Why: the PR #174 review cost about 111k tokens, mostly on booting, building, seeding data and taking screenshots the implementer had already taken (owner decision, 2026-09-12).* The subagent follows the `mattpocock-skills:code-review` skill for the Standards and Spec axes, with the PR's merge-base with `origin/main` as the fixed point. *Why: a dedicated review session fills its context over a wave of PRs and reviews worse each time. It also depends on the owner remembering to `/clear` it. A subagent starts empty every time (owner decision, 2026-09-12).*
- Design-doc PRs go in a fixed order: Fable writes the design, a Sonnet pass cross-checks every `UiState` / intent / token / component name against current code (code wins; genuinely new things are marked "(new)"), then Opus high reviews. *Why: PR #171 got ~20 findings that were almost all spec-vs-code drift, not new bugs.*
- Merge is rebase-only, linear history, CI required. The orchestrator never blocks its own turn on `gh run watch` — it merges when the CI notification or the session's report arrives.
- Auto-merge (`gh pr merge --rebase --auto`) only works while checks are still pending. If the PR is already CLEAN, GitHub refuses to enable it ("Pull request is in clean status"). An armed auto-merge also did not fire on PRs #181 and #185. Check the state first with `gh pr view <N> --json mergeStateStatus`: merge directly with `gh pr merge <N> --rebase` when it is CLEAN, and arm auto-merge only while checks are pending. After arming, the implementing session confirms with `gh pr view <N> --json state` once CI passes and pings the orchestrator if the PR is still open.

## Between tickets

- A cycle is closed only when all of this is done, in order. First the PR is merged. Then the peer shuts down its emulator (`adb -s <serial> emu kill`), leaves its worktree clean and removes it (`git worktree remove <path>`). Then its local branch is deleted (`git branch -D <branch>`) and `git worktree prune` runs. Last, any throwaway review worktree under a scratchpad is removed. The orchestrator checks `git worktree list` before reporting the session as free. If a peer is gone, the orchestrator removes the leftovers itself, but only after confirming that no live session uses them.
- Also at cycle close, the orchestrator asks whether the merged work made an architecture or domain decision, or changed a domain term. If it did, it files a small docs ticket that loads `mattpocock-skills:domain-modeling` and adds or updates the ADR in `docs/adr/`, `CONTEXT.md`, or both. An ADR that the shipped behavior contradicts is fixed the same way. *Why: the Release-on-demand decision in #175 shipped without an ADR, and ADR 0015 kept saying "Saltar" after the screen moved to "Listo"; #187 caught both (2026-09-13).*
- A peer session carries exactly one ticket. When its PR is merged, the orchestrator closes the cycle (cleanup above) and kills the pane: kill the peer's `claude` pid, then `kill -HUP` its parent `zsh`. It never sends a second ticket to the same session. When every PR of the wave is merged, it invokes `/wave` with the next wave's tickets. *Why: a session carrying the previous ticket's context drifts and costs more; fresh panes per wave replaced the owner reconfiguring each session by hand (owner decision, 2026-09-16).*
- At session start, the orchestrator searches memory (`mem_search`) for past dispatch gotchas before the first dispatch of the session. *Why: on 2026-09-12, `@tokens` was dispatched into the main checkout without `ANDROID_SERIAL` because past lessons weren't searched first.*
