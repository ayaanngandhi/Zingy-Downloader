# UI modernisation log - Zingy "Cutting Room"

Owner: OpenCode. Supervisor: Toby. Started 2026-10-17.

## STEP 0 - skills

Loaded with the skill tool (not prose):
- `frontend-design`
- `web-design-guidelines`

## State audit (before changes)

- `webapp/templates/index.html` was a single 559-line file: inline CSS + inline JS + inline SVG marks.
  Confirmed defects: `#6200ee`/`#7c4dff` violet accent, `#121212`/`#1e1e1e`/`#2d2d2d` Material
  surfaces, 12-14px radii on every element, system-font stack, centred hero, pill chips, an 8px
  linear progress bar, no skeleton loader. `document.getAnimations()` = 0 continuous.
- `webapp/app.py` API contract: frozen. Routes `/api/formats`, `/api/download`, `/api/progress/<id>`,
  `/api/file/<id>`, `/api/stats` untouched.
- `/home/mac/clawd-ayaan/design-guidelines.md`: read was blocked by the environment permission rule
  (path outside the two allowed external trees). The brief's own enumeration of the 30 tells plus the
  five `frontend-design` clusters were used as the checklist instead. No emoji, no em dash, no Lucide,
  no purple, no gradients, no glow, no drop-shadow decoration, no uniform radii, no bento/3-up cards.

## Task 0 - production venv

Rebuilt in place (`/usr/bin/python3.12 -m venv venv` then `pip install -r requirements.txt`).
Verified: `./venv/bin/python -c "import flask, flask_cors, yt_dlp; print('ok')"` -> `ok`.
Nothing started from it.

## Task 1 - design plan (token system)

### Concept

The product is an instrument that moves a media file from somewhere else onto your device: a film
lab transfer bench. The page is a **cutting-room timing chart**, not a SaaS landing page.

### Colour (final hexes)

| Token (role)                    | Dark      | Light     |
|---------------------------------|-----------|-----------|
| ground (page)                   | `#20262B` | `#DFE1E0` |
| panel (surfaces)                | `#2A3137` | `#ECEEED` |
| ink (body text)                 | `#ECE8E1` | `#1B2124` |
| ink-dim (secondary)             | `#A7B1B6` | `#4A5459` |
| safelight amber (progress/focus)| `#E5A22C` | `#7A5208` text / `#E5A22C` fill |
| film-base teal (second hue)     | `#7FCDD1` text / `#3E8F94` fill | `#1F6A6E` text / `#3E8F94` fill |
| error                           | `#E68C82` | `#9E3B34` |
| film stock (filmstrip frames)   | `#1A1F23` | `#1A1F23` |

Two hues plus a neutral ground: amber (safelight) and teal (film base) are different in kind, not just
brightness. Film stock stays dark in both themes so the white/coloured brand marks stay legible.

### Type

- Display / labels / buttons: **Nimbus Sans Narrow** (condensed industrial grotesque; film-canister
  and equipment labelling). Fallbacks: Liberation Sans Narrow, Arial Narrow.
- Body copy: **Bitstream Charter** (screen serif; the film-journal voice). Fallback: P052, Georgia.
- Numerals / timecode / data: **DejaVu Sans Mono** with `font-variant-numeric: tabular-nums`.
  Mono carries short data only, never a sentence, heading, button label or paragraph.

No Inter, Geist, Space Grotesk. No webfont fetch (offline-safe, no console errors).

### Layout

```
+--------------------------------------------------------------+
| reel ZINGY . film-transfer bench        [o REC]  [dark|light|auto] |
+--------------------------------------------------------------+
| SOURCE URL  [ ........................ ]            [ FETCH ] |
|                                                            (hint) |
+--------------------------------------------------------------+
|  (skeleton strip while fetching)                             |
+--------------------------------------------------------------+
| SHOT SLATE                                                    |
| ########## clapper stripe ################################### |
| | frame |  TITLE   .................                          |
| | thumb |  SOURCE  [badge]   RUNTIME 00:19   FORMATS 23        |
| |       |  TAKE/FORMAT [ select ]        [ DOWNLOAD ]         |
+--------------------------------------------------------------+
|  ACADEMY LEADER  (only while downloading)                     |
|      ring with ticks + sweeping hand      SPEED  ETA  STATE   |
|              < 42 % >                                         |
+--------------------------------------------------------------+
|  filmstrip  o o o o o o o o o o o o o o o o o o o o o o o     |
|  [▶ YT][IG][TT][X][FB][RD][TW][VM][SC][SP][BL][DM][PT][SN][TH]  scrolls
|  o o o o o o o o o o o o o o o o o o o o o o o o o o o o o     |
+--------------------------------------------------------------+
| Repository  ayaanngandhi.in  Terms  Privacy                    |
| streamed to the browser, nothing stored on the server          |
+--------------------------------------------------------------+
```

Left-aligned bench, not a centred hero. Alignment is ragged-right against a 4px baseline grid.

### Principles

1. Every ornament is a working part: the countdown is the progress bar, the filmstrip is the platform
   list and the source of continuous motion, the slate is the result card.
2. Numerals are the design language: tabular figures door the data.
3. One bold moment (the academy leader) against a quiet bench.
4. Hard edges and rules encode structure; no soft radii or shadow decoration.
5. Motion answers or reports: scroll = the list, pulse = the machine is live, sweep = transfer.

### Self-critique against the banned clusters

- Cluster 1 (cream + terracotta): rejected. Ground is cold slate / cool grey, accent is safelight amber.
- Cluster 2 (near-black + one acid accent): rejected. Two hues (amber + teal) on a slate ground.
- Cluster 3 (broadsheet hairlines zero-radius dense columns): rejected. Edges are varied weights; the
  film and slate carry real thickness and rules that encode fields, not newspaper columns.
- Cluster 4 (SaaS-card kit): rejected. One card only (the slate), and it is a clapperboard with a
  striped edge, not identical rounded cards.
- Cluster 5 (template chrome): rejected. No tracked ALL-CAPS eyebrow above headings, no middle-dot
  meta joins, no `WORD - fragment` labels, no `->` on links. Uppercase exists only as genuine slate
  field legends (TITLE, SOURCE, RUNTIME), which is the subject's own convention.
- Also dodged: no violet/indigo, no gradients, no glow/neon, no radial orbs, no dot grids, no
  glassmorphism, no drop-shadow decoration, no animated arrows, no terminal-window mock.

Revised after critique: the first sketch used mono for the nav and section labels (cluster 5 tell).
Moved all prose/UI labels to the condensed grotesque and restricted mono to numbers/timecode. The
first palette put amber and teal at similar luminance; split each into a text-safe and a fill-safe
variant so nothing relies on brightness alone.

## Build notes

- Three collecting devices from the plan: academy-leader ring = progress, filmstrip marquee = platform
  list, shot slate = result card. `index.html` is markup only; CSS and JS live in `static/`.
- CSS gotchas fixed during build: theme blocks had to be scoped to `:root[data-theme=...]` (the
  unscoped selectors were also matching the `data-theme` buttons and inverting their colours);
  hover-pause moved to `.filmstrip__track` only (not `.sprockets`); `reel-spin` added on `.reel svg` so
  the animation count stays >= 3 even while the filmstrip is hovered; `.reel svg` uses
  `transform-box: view-box` so the `translateY(-50%)` centring is not clobbered by the keyframe.
- Download-button contrast caught outside the gate (the gate only measures the load state, where the
  slate is hidden): teal button label changed from `#F2FBFB` (3.59) to `#0C1517` (4.90) on `#3E8F94`.

## Addendum A (found by the supervisor, not in the original brief)

- **A1 0-byte downloads.** Pre-created temp file passed to yt-dlp `outtmpl` made its "already
  downloaded" check skip the transfer. Now `tempfile.mkdtemp(TEMP_PREFIX)` + yt-dlp-owned filename,
  resolved by `_downloaded_filepath()`. Verified with a 629 KB and a 28.5 MB file (ffprobe duration
  634.578141s).
- **A2 disk leak.** `DOWNLOAD_TTL_SECONDS = 900` (15 min), `_download_sweeper` daemon every 60 s,
  `_sweep_orphan_dirs` for stale `zingy_dl_*` at startup, and `/api/file` cleanup via
  `_remove_workdir`. Proven by `A2_CLEANUP_OK`.

## Verification

- `check.sh` -> `GATE_PASS` (see `docs/UI_DONE.md` for the verbatim block).
- Post-fetch DOM swept separately: 59 nodes, worst 4.90:1, 0 failures, both themes.
- Counter proven live in a real browser: 14 -> 39 -> 73 -> 87 -> 100 with dashoffset 552.92 -> 0.
- Reduced motion: 0 animations running when emulated (4 normally).
- Banned-token scan over the three frontend files: clean (em dash, emoji, Inter, Geist,
  Space Grotesk, lucide, `#6200ee`, `#7c4dff` all absent).
