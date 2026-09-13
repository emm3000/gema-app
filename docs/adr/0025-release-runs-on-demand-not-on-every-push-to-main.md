---
status: accepted
---
# Release runs on demand, not on every push to main

Before #175, `Release` ran on every push to `main`, so `bundleRelease` and its
R8 pass executed on every merge whether or not a release was intended. PR #177
moved the trigger to `workflow_dispatch` (with a `track` input) plus `v*` tags,
recorded in `.github/workflows/release.yml` and `docs/release.md`. Shipping a
build is now `gh workflow run Release` or pushing a `v*` tag; a plain merge to
`main` starts nothing.

## Consequences

The AAB that used to exist after every merge no longer does: catching an R8 or
minify break now depends on CI's `assembleRelease` gate on pull requests, not
on the `Release` workflow's `bundleRelease`. If `assembleRelease` and
`bundleRelease` ever diverge in what they minify or package, a break could
reach a tag before it is seen.

The trade-off was accepted because `Release` also builds the signed bundle and,
once `PLAY_PUBLISH_ENABLED` is set, publishes it — work with no reason to run
on commits nobody intends to ship.
