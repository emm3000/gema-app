# Play Console preparation docs

Repository-side deliverables for issue #21 (Play Console: account, listing
and policy forms), part of #1. Assets (icon, feature graphic, screenshots)
are out of scope for this set and wait on the first signed AAB from #15.

## Files and what they feed

| File | Feeds |
|---|---|
| `listing.md` | Grow > Store presence > Main store listing: app name, short and full description, category, tags, contact email. |
| `privacy-policy.md` | The public privacy policy URL required by App content > Privacy policy. Publish it via GitHub Pages (steps below) and paste the resulting URL into Play Console. |
| `data-safety.md` | App content > Data safety form: every required question, its answer, and a one-line justification. |
| `content-rating.md` | App content > Content ratings: the IARC questionnaire answers and the expected resulting rating. |
| `target-audience.md` | App content > Target audience and content: age group, children's-appeal answer, ads declaration. |

## Publishing `privacy-policy.md` via GitHub Pages

Recommendation: **use a `docs/` folder on `main`**, not a separate
`gh-pages` branch. It needs no extra branch to keep in sync, the file stays
versioned alongside the app, and Play Console only needs a stable URL —
both options satisfy that equally well, but `docs/` is one fewer moving
part for a solo maintainer.

Steps:

1. Push this `docs/play/privacy-policy.md` file to `main` (or merge the PR
   that adds it).
2. In the GitHub repository, go to **Settings > Pages**.
3. Under **Build and deployment > Source**, choose **Deploy from a
   branch**.
4. Under **Branch**, select `main` and folder **`/docs`**, then **Save**.
5. Wait for GitHub to publish the site (a banner on the same page shows the
   published URL once it's live, typically within a minute or two).
6. The privacy policy will be reachable at:
   `https://emm3000.github.io/gema-app/play/privacy-policy.md`
   GitHub Pages serves Markdown as plain text, not rendered HTML; if Play
   Console review expects a normal web page, wrap the same content in a
   minimal `docs/play/privacy-policy.html` (or add `docs/index.html` that
   links to it) and point Play Console at that URL instead. The Markdown
   file's content is what to reuse verbatim.
7. Paste the published URL into **App content > Privacy policy** in Play
   Console.

### Alternative: `gh-pages` branch

Only prefer this if `docs/` is later needed for something unrelated to
Pages (it currently also holds `docs/adr/` and `docs/agents/`, which do not
need to be public). To use it:

1. `git checkout --orphan gh-pages`, keep only the published files, commit,
   and push `gh-pages` to `origin`.
2. In **Settings > Pages**, set **Source** to **Deploy from a branch**,
   **Branch** `gh-pages`, folder `/ (root)`.
3. Keep the branch manually in sync with `docs/play/privacy-policy.md` on
   `main` (e.g. `git checkout gh-pages && git checkout main -- docs/play/privacy-policy.md`
   after every edit), since it does not update itself.

This repo already stores unrelated files under `docs/`, so the `docs/`
folder option is the one actually recommended above.
