# CNEB reference material — Educación Primaria

Seed-ready data for the CNEB (Currículo Nacional de la Educación Básica,
Minedu Perú) competencies of Educación Primaria (EBR, grades 1°-6°), for
issue #7 (CNEB seed + Worked Competencies). Gathered 2026-09-10.

## Sources

- **Currículo Nacional de la Educación Básica (CNEB), 2016** — approved by
  RM N.° 281-2016-MINEDU.
  https://www.minedu.gob.pe/curriculo/pdf/curriculo-nacional-2016.pdf
  - Cuadro N.° 1 (áreas, competencias y niveles), pp. 87-88 — confirms
    Educación Primaria has 9 curricular areas and 30 competencies total
    (28 area competencies + 2 transversal).
  - Escala de calificación, pp. 101-102 — literal scale AD/A/B/C and their
    official descriptions.
  - Tabla N.° 6 (horas Inglés) and Tabla N.° 7 (horas Castellano L2), p. 89-90
    — grade and modality conditionality for those two areas.
- **Programa Curricular de Educación Primaria** — annexed by
  RM N.° 649-2016-MINEDU, modified by RM N.° 159-2017-MINEDU.
  https://www.minedu.gob.pe/curriculo/pdf/programa-nivel-primaria-ebr.pdf
  - Per-area competency definitions and headers used to verify exact
    wording, pp. 9, 56, 72-192.
  - Transversal competency "Gestiona su aprendizaje de manera autónoma",
    p. 192; "Se desenvuelve en entornos virtuales generados por las TIC",
    p. 182.
- `docs/siagie/README.md` (this repo) — SIAGIE grades template structure,
  used to align each Area with its SIAGIE Excel sheet tab and to confirm
  that SIAGIE numbers competencies per Area ("Competencia 01 NL",
  "Competencia 02 NL", ...), not with a single curriculum-wide ordinal.

Both PDFs were fetched and text-extracted directly; no blog or secondary
compilation was used as the source of truth for a competency name. A
secondary site (sites.minedu.gob.pe) was checked only as a pointer and its
wording was discarded where it diverged from the PDFs (see Ambiguities).

## Mapping to SIAGIE area codes

`docs/siagie/README.md` lists the sheet tabs observed in a real primary
grades template: `CAST SEGNL, CIENC TEC, COMU, EFIS, EREL, INGLES EXT, MATE,
PPSS, GEST AU, ...` (list truncated with `...` in the source, so it is not
guaranteed exhaustive).

`docs/cneb/primary.json` uses an underscore variant of each observed tab as
`code` (`CAST_SEGNL`, `CIENC_TEC`, `INGLES_EXT`) or the tab verbatim
(`COMU`, `EFIS`, `EREL`, `MATE`, `PPSS`) so the seed can join 1:1 with an
imported template's sheet name once whitespace is normalized.

| `code` in `primary.json` | Official area name | Observed SIAGIE tab |
|---|---|---|
| `COMU` | Comunicación | `COMU` |
| `CAST_SEGNL` | Castellano como segunda lengua | `CAST SEGNL` |
| `INGLES_EXT` | Inglés como lengua extranjera | `INGLES EXT` |
| `MATE` | Matemática | `MATE` |
| `CIENC_TEC` | Ciencia y Tecnología | `CIENC TEC` |
| `PPSS` | Personal Social | `PPSS` |
| `EFIS` | Educación Física | `EFIS` |
| `ARTE` | Arte y Cultura | not observed in the sample tab list (see Ambiguities) |
| `EREL` | Educación Religiosa | `EREL` |

`GEST AU` in the observed tab list is **not a curricular area**. It is the
transversal competency "Gestiona su aprendizaje de manera autónoma", carried
in `primary.json` under `transversalCompetencies[1].siagieCode`. The other
transversal competency, "Se desenvuelve en entornos virtuales generados por
las TIC", has no confirmed SIAGIE tab (`siagieCode: null`) — see Ambiguities.

## Competency numbering

CNEB does **not** number competencies with a single curriculum-wide ordinal
(there is no "Competencia 1: ...Competencia 30" in the official text).
Numbering is only per Area, in the fixed order the CNEB presents them, and
that per-area order is exactly what SIAGIE's own column headers use
(`docs/siagie/README.md`'s PPSS example: "01 Construye su identidad, 02
Convive y participa ..., 05 Gestiona responsablemente los recursos
economicos" matches the order below). `primary.json`'s `number` field is
therefore the per-area SIAGIE ordinal, matching issue #7's "SIAGIE ordinal"
requirement, not a global CNEB index.

## Ambiguities and unverified items

- **`ARTE` SIAGIE tab unconfirmed.** `docs/siagie/README.md`'s observed tab
  list ends in `...`, so Arte y Cultura's real sheet tab name was not in the
  sample. Confirm against a real exported `.xlsx` before relying on `ARTE`
  as the exact tab string.
- **TIC transversal competency has no confirmed SIAGIE tab.** Only
  "Gestiona su aprendizaje de manera autónoma" was observed as `GEST AU`.
  Whether/how "Se desenvuelve en entornos virtuales generados por las TIC"
  appears in a real SIAGIE grades template (own tab, embedded in another
  area's sheet, or absent from Primaria's template) is unverified — no
  official SIAGIE technical/user manual was found on minedu.gob.pe
  describing sheet-tab semantics.
- **RVM N.° 094-2020-MINEDU and RVM N.° 033-2023-MINEDU were not
  verifiable.** RVM 094-2020's only located PDF returned 403 Forbidden; RVM
  033-2023 could not be located at all. Whether either resolution changed
  the AD/A/B/C wording or any competency name after the 2016 CNEB is
  **unverified** — `achievementScale` and the competency names in
  `primary.json` are sourced to the 2016 CNEB and its 2016 Programa
  Curricular annex only.
- **Minor wording drift inside the official PDFs themselves** between each
  area's summary table (Cuadro N.° 1) and its detailed competency-definition
  section header, e.g. Ciencia y Tecnología competency 1 ("...para construir
  conocimientos" vs. "...para construir sus conocimientos"), Matemática
  competency 3 word order ("movimiento, forma y localización" vs. "forma,
  movimiento y localización"), and Comunicación competency 1 ("se comunica
  oralmente en lengua materna" vs. section header "se comunica oralmente").
  `primary.json` uses the summary-table (Cuadro N.° 1) wording throughout,
  since that is the wording presented as the authoritative area/competency
  list; the detailed-section variants are noted here for traceability.
- **Educación Religiosa is also called "área de Religión"** in its own
  exemption clause (Ley N.° 29635) on the same page that titles it
  "Educación Religiosa". `primary.json` uses "Educación Religiosa" as the
  canonical name, matching the area title and the observed `EREL` tab.
- **Pre-2016 "Arte" vs. "Arte y Cultura" naming history is unverified** —
  would require the pre-2016 DCN, which was not fetched; irrelevant to the
  current CNEB but noted in case an older SIAGIE template surfaces "ARTE"
  without "Cultura".

## `primary.json` structure

- `areas[]` — one entry per curricular area: `code` (join key to a SIAGIE
  sheet tab), official `name`, `grades` (1-6, all apply to every primary
  grade for the areas in this seed), `conditionality` (`null` or a note for
  EIB-only / non-EIB-only / opt-in areas), and `competencies[]` (`number` =
  per-area SIAGIE ordinal, `name` = official Spanish name verbatim).
- `transversalCompetencies[]` — the two cross-curricular competencies (TIC,
  autonomous learning), not tied to one area.
- `achievementScale[]` — the literal scale AD/A/B/C with the official
  Minedu description of each level.
