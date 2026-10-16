# Zingy web UI — modernisation brief

**Task owner:** OpenCode. **Supervisor:** Toby. **Repo:** `/home/mac/projects/android_downloader`.

FIRST: verify the current state before changing anything. Read these in full:
- `webapp/templates/index.html` (the current UI, ~559 lines, inline CSS + JS)
- `webapp/app.py` (Flask API you must NOT break)
- `/home/mac/clawd-ayaan/design-guidelines.md` (the owner's anti-vibecode list — 30 tells)

Line numbers are deliberately omitted; confirm each defect yourself. If something below is already
fixed, say so and skip it.

---

## STEP 0 — load these skills FIRST, with the tool, before anything else

Naming a skill in prose does NOT load it. Call the skill tool explicitly and confirm in your reply
which ones loaded:

```
skill({ name: "frontend-design" })
skill({ name: "web-design-guidelines" })
```

`frontend-design` is a **design-lead** skill, not a polish pass. Obey its process: brainstorm a token
plan (colour / type / layout / principles) and self-critique it against its own list of AI-default
looks **before** writing code.

---

## The problem (verified by the supervisor, not a guess)

The live page at `http://127.0.0.1:4321/` was measured with an independent gate. Current scores:

- **0** continuous animations (`document.getAnimations()` → 0 running). The page is dead still. This is
  the single biggest reason it reads as old.
- **Contrast failures in BOTH themes**: 4 of 26 text nodes in dark, 6 of 26 in light. The worst are the
  footer links — accent `#6200ee` on `#121212` = **2.46:1**. One separator glyph measures **1.24:1**.
  Floor is 4.5:1 for body text.
- Purple `#6200ee` on near-black `#121212`, Material-default surfaces `#1e1e1e`/`#2d2d2d`, soft 12–14px
  radii on everything, and a system-font stack. That is the canonical "AI wrapper landing page" look:
  violet accent, dark default, centred hero, pill chips.

**Pixel-difference from the current design must be at least 0.60 of pixels per viewport.** The measured
baseline-vs-itself score is 0.0000. Anything under 0.15 counts as a refinement and is a FAILURE for this
task. A clean, tidy, well-organised version of the current palette is a failure.

---

## The concept you must build — "The Cutting Room"

Ground the design in what the product actually is: an instrument that takes a media file from somewhere
else and lands it on your device. The visual language is a **film lab / cutting-room timing chart**, not
a SaaS landing page.

Three required devices. These are the design, not decoration — each must do a real job:

1. **Academy-leader countdown as the download progress indicator.** A circular ring with a sweeping hand
   and crosshair tick marks around the rim, large tabular timecode numerals in the centre showing the
   live percentage, with speed and ETA as small data beneath. It replaces the current 8px linear bar.
   It must be driven by the real `/api/progress/<id>` values, not faked. This is the product demo
   (the owner's list flags "no real product demos" as a tell).
2. **The platform list as a filmstrip.** A horizontal strip with sprocket perforations along the top and
   bottom edges, the 15 platform marks each sitting inside its own frame, **scrolling continuously**
   (marquee, pauses on hover). This is what supplies the continuous motion the page currently lacks.
3. **The fetched-video card as a shot slate.** Title, source platform, duration and the format selector
   presented like a clapperboard/slate: a striped clapper edge, a field-labelled grid. It stops being a
   generic rounded card.

**All numeric data** (duration, percentage, file size, speed, ETA, format counts) is set in tabular
figures. Numerals are part of the design language. Mono may carry short data values only — never a
sentence, heading, button label or paragraph.

Use the existing brand SVG marks already in `index.html` as the platform icons. Do not replace them with
generic icons or emoji.

### Hard bans — a build containing any of these is a failure

From the owner's `design-guidelines.md` (all 30 apply). The ones you are most likely to reach for:

- **No emoji anywhere** in the UI or in any string you author.
- **No Lucide icons** and no Lucide-looking stock icons. Use the existing brand SVGs + your own marks.
- **No em dashes** (`—`) in any string you author. Use a hyphen or restructure the sentence.
- **No Inter, Geist, or Space Grotesk.** Pick a distinctive face and say why in a comment.
- **No purple-and-black.** `#6200ee`, `#7c4dff`, violet/indigo ramps are banned outright.
- **No harsh gradients, no rainbow, no neon, no pastels, no radial orbs, no dot-grids, no glassmorphism.**
- **No drop shadows** as decoration, **no soft/uniform corner radii** on everything, **no bento grid**,
  no 3-in-a-row feature cards, no terminal-window mock, no animated arrows.

Additional bans, from `frontend-design`'s own list of AI-design clusters — **do not build any of these
five looks**, they are the defaults a model reaches for rather than choices:

1. warm cream ground (near `#F4F1EA`) + high-contrast serif display + warm clay/terracotta accent
   (near `#D97757`);
2. near-black ground + a single acid-green or vermilion accent;
3. broadsheet/newspaper: hairline rules, zero border-radius everywhere, dense columns;
4. the SaaS-card kit: identical rounded cards, one radius regardless of hierarchy, the same soft
   `rgba(0,0,0,.1)` shadow under each, gradient washes as decoration;
5. template chrome applied regardless of subject: a tracked-out ALL-CAPS eyebrow above every heading,
   meta joined by middle dots, `WORD — fragment` labels, mono small labels, `→` appended to links.

**Colour requirement:** at least **two distinguishable hues plus a neutral ground** — a theme needs
elements that differ in kind, not only in brightness. A single-accent monochrome ramp is a failure. A
defensible starting set (you may adjust, but state your final 4–6 hexes and show they dodge the clusters
above): ground `#20262B` cold slate, light ground `#DFE1E0` cool grey, safelight amber `#E5A22C`,
film-base teal `#3E8F94`, error `#C4514A`.

---

## Files and structure

Split the single 559-line file. Keep the Flask template pipeline:

- `webapp/templates/index.html` — markup only.
- `webapp/static/zingy.css` — all styles.
- `webapp/static/zingy.js` — all behaviour.
- Keep `webapp/static/favicon.svg` where it is.

### Functionality that MUST still work (verify each one for real, in a browser)

- Paste URL → **Fetch** → `POST /api/formats` → title, thumbnail, platform badge and the format list
  (3 presets then up to 20 real formats) all populate.
- **Download** → `POST /api/download` → poll `GET /api/progress/<id>` → on `completed`, trigger
  `GET /api/file/<id>` and the file saves. Progress % must drive the countdown ring.
- Error states: unsupported platform, bad URL, and a failed download must all show a readable message.
- Theme toggle: dark / light / auto, persisted in `localStorage` under the key **`zingy-theme`**
  (keep that exact key), including the `prefers-color-scheme` listener for `auto`.
- Footer links to the GitHub repo and `ayaanngandhi.in`.
- **Add a skeleton loader** for the fetch-in-flight state (the owner's list flags its absence).

### `app.py` changes — additive only, keep the API contract frozen

The Android app is a second front end over the same engine. **Do not change any existing route, HTTP
method, or JSON key** in `/api/formats`, `/api/download`, `/api/progress/<id>`, `/api/file/<id>`,
`/api/stats`. Do not touch `detect_platform()`, `SUPPORTED_PLATFORMS`, the `extractor_args` workarounds,
or the `visit_logs.json` record shape.

You may only ADD two routes, `GET /terms` and `GET /privacy`, each rendering a real, honest page of
plain-language terms and privacy policy for this tool (what it collects, what it does not store, what
the visit log contains, no warranty, acceptable use). Short and truthful beats long and boilerplate.
Link both from the footer. Templates for them go in `webapp/templates/`.

---

## HARD operational constraints — violating these breaks live production

1. **Port 4321 is LIVE PRODUCTION** (`zingy.ayaanngandhi.in`, served by the systemd unit
   `personal-website.service`). **Never start, stop, restart, reload, or kill anything on 4321.** Do not
   use it as evidence of your work — it serves the OLD build. Do not edit anything under
   `/home/mac/production/zingy/` except the one exception in task 0 below.
2. **NEVER run `pkill -f`, `pkill`, `killall`, `fuser -k`, or any process-name pattern.** This box runs
   several unrelated live services and a pattern kill takes them all down. To stop your own server, use
   its PID only:
   `pids=$(ss -ltnp | grep ':4322' | grep -oP 'pid=\K[0-9]+'); [ -n "$pids" ] && kill $pids`
3. **Run your dev server on port 4322 only:** `cd webapp && ./venv/bin/python -c "import app; app.app.run(host='127.0.0.1', port=4322)"`
   or `PORT=4322`. Never edit the `4321` literal in `app.py`.
4. **Do not run `git commit`, `git push`, `git checkout`, `git reset`, or `git stash`.** Leave the work
   uncommitted in the working tree; the supervisor commits.
5. Do not modify the Android/Gradle side, `build_apk.sh`, or any `.kts` file.

---

## Tasks

**0. Repair the production venv (the ONLY edit allowed under `~/production/`).**
`/home/mac/production/zingy/webapp/venv` has lost its interpreter and has no packages installed. Rebuild
it in place from `/home/mac/production/zingy/webapp/requirements.txt` using `/usr/bin/python3.12`
(`/usr/bin/python3.12 -m venv venv` then `./venv/bin/pip install -r requirements.txt`). Verify with
`./venv/bin/python -c "import flask, flask_cors, yt_dlp; print('ok')"`. Do not start anything from it.

**1. Design plan first.** Following `frontend-design`'s process, write your token plan (4–6 named hexes,
typefaces + roles, layout concept with a short ASCII wireframe, principles) into `docs/ui-log.md`, and
state explicitly what you revised after checking it against the banned clusters. Only then write code.

**2. Build it.** Split the files as specified above and build the Cutting Room design with all three
required devices.

**3. Self-check against the supervisor's own gate — ONE command:**

```
bash /home/mac/clawd-ayaan/zingy-redesign/check.sh
```

That captures your UI at both viewports and themes and runs the gate against the frozen baseline.
It must print `GATE_PASS`. Iterate until it does. If it prints `GATE_FAIL`, fix the specific failing
lines it names, then run it again. Inspect `/home/mac/clawd-ayaan/zingy-redesign/new-*-DIFF.png` to
see where your design still matches the old one (bright = still identical).

Access to `/home/mac/clawd-ayaan/zingy-redesign/**` and `/home/mac/production/zingy/**` is permitted;
use the absolute paths. Do not edit `check.sh`, `capture.py`, or `gate.py`.

**4. Prove the functionality in a real browser**, not with curl alone. Drive a real URL through Fetch
and a real download end to end, and confirm the file actually arrives. Use a small public video so the
test is quick.

**5. Write `docs/UI_DONE.md`** with: the token plan; the gate output verbatim; the measured contrast
numbers; the list of continuous `@keyframes` by name; the functional evidence; and an explicit
"Ax-1 … Ax-6" line per acceptance criterion below marking PASS or FAIL. If you could not verify a
criterion, write FAIL and say why. **Do not write `docs/UI_DONE.md` unless the gate prints GATE_PASS.**

---

## Acceptance criteria — observable evidence, not adjectives

- **Ax-1 Structural change.** Pixel difference vs baseline ≥ 0.60 at 1440×900 and ≥ 0.50 at 390×844, in
  both themes. Evidence: the gate's `changed=` numbers.
- **Ax-2 Contrast.** Zero text nodes below 4.5:1 (body) / 3:1 (≥24px, or ≥18.66px bold), in both themes,
  measured on every leaf text node.
- **Ax-3 Continuous motion.** ≥ 3 `@keyframes` animations running with `iterations: Infinity` in both
  themes, all meaningful (filmstrip scroll, countdown sweep, and one more you choose), all disabled
  under `prefers-reduced-motion: reduce`.
- **Ax-4 Legibility floor.** No rendered text below 10px; line length for body copy under 80 characters.
- **Ax-5 No console errors** on load, on fetch, or on download, in either theme.
- **Ax-6 Banned-token scan clean.** Zero emoji, zero em dash in authored strings, `Inter`/`Geist`/
  `Space Grotesk`/`lucide` absent from all three files, `#6200ee`/`#7c4dff` absent.

Close with: if you could not verify a criterion, say so explicitly rather than claiming success.

---

# ADDENDUM A (added by the supervisor after the run started — highest priority)

**This overrides Task 4's "quick test": the download path is currently BROKEN IN PRODUCTION and
repairing it takes priority over every visual item in this brief.**

## A1. Every download produces a 0-byte file. Fix the root cause.

Verified end to end by the supervisor against the live app on 4321:

```
POST /api/download        -> {"success": true, "download_id": "c17fb2e7"}
GET  /api/progress/<id>   -> {"status": "completed", "progress": 100, "title": "Me at the zoo"}
GET  /api/file/<id>       -> HTTP 200, bytes=0
```

A trivially repro needed no HTTP at all:

```
tmp = tempfile.NamedTemporaryFile(suffix='.mp4', delete=False); tmp.close()
ydl_opts = {'outtmpl': tmp.name, ...}
# yt-dlp output:
#   [download] /tmp/tmpXXXX.mp4 has already been downloaded
#   [download] 100% of      0.00B
```

**Root cause:** `download_video_task()` in `app.py` creates the temp file and *closes it with
`delete=False`*, leaving a real 0-byte file on disk. It then hands that same path to yt-dlp as
`outtmpl`. yt-dlp's "already downloaded" check sees an existing file at the target path and
**silently skips the transfer**, then reports `finished`. The progress hook sets `completed`, the
title is populated because `extract_info` did run, and `/api/file` streams 0 bytes. Nothing errors
anywhere in the chain.

**Required fix:** never hand yt-dlp a path that already exists. Create a unique *directory* and let
yt-dlp own the filename inside it, or generate a unique path and `os.unlink()` it before the download
starts. Do not simply add `overwrites: True` and leave a pre-created file in place if you can avoid
it — the directory approach also gives you a place to clean up.

**Acceptance evidence for A1 (all three, or it is not fixed):**
- The downloaded file is **non-zero**. Report the exact byte count.
- `file <path>` or `ffprobe -v error -show_entries format=duration,size <path>` identifies it as a
  real MP4 with a duration. Paste that output.
- A full round trip through the HTTP API returns **bytes > 0** from `/api/file/<id>`, and the saved
  file plays. Test with a small public video **and** with one long enough to produce a multi-megabyte
  file, since a 19-second clip is a weak test.

## A2. Abandoned downloads leak disk — and `/` has ~2 GB free.

The temp file is only unlinked when a client actually finishes `/api/file/<id>`. Any download whose
progress is polled but never collected (closed tab, phone sleeping, client crash) leaves its **full-size
file on `/` forever** — there is no TTL and no sweeper. The box's root filesystem is 93-99% full, so
this is a real outage waiting to happen once A1 is fixed and files become full-size.

Add cleanup: unlink temp files for downloads that finish and are never collected after a short TTL, and
sweep the download directory at startup for anything left over from a previous run. Keep it small and
obvious; a background thread or a check on each new download is fine. State the TTL you chose and how
you proved cleanup works (e.g. start a download, never call `/api/file`, show the file is gone after
the TTL).

## A3. Note for your report

Both A1 and A2 are pre-existing defects that the supervisor found while measuring; they were not in the
original brief. Say so in `docs/UI_DONE.md`, and report them as fixed-in-this-run rather than implying
the brief always covered them.
