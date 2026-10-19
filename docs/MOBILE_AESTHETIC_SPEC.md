# Spec — bring the Zingy Android app up to the "Cutting Room" aesthetic

## Goal

The Zingy **web** app was redesigned on 2026-10-17 (commit `2706357` "Redesign web UI as a film-lab
transfer bench", then `45df809` polish). The **Android app is still on the old default look** — stock
Material purple (`0xFF6200EE` primary, `0xFF03DAC5` secondary) with a `0xFF121212`/`#F5F5F5` ground.

Restyle the Android app so it reads as the same product as the web app.

## Read these first — they are the design authority, not this document

In this repo:

1. `webapp/static/zingy.css` — the complete design language. Read all of it (746 lines).
2. `webapp/templates/index.html` — how the panels are composed.
3. `docs/UI_POLISH.md` — the design rules and the review defects that were fixed.

Do not invent a new look. Port what is there. Where this document and the CSS disagree, the CSS wins.

## The design language

A cutting-room timing chart for a media transfer bench. An **instrument panel**: condensed industrial
display type, a real screen serif for body copy, tabular mono for anything numeric. Hairline rules,
sharp corners, exactly **one** accent.

### Palette (exact values from `zingy.css`)

Map these to a Compose `ColorScheme`. Both themes are required — the app already has a dark/light/auto
setting and a `ThemeButton`, so both branches must be real.

Dark (`:root[data-theme="dark"]`):
- ground `#20262B` · panel `#2A3137` · panel2 `#333C43`
- ink `#ECE8E1` · inkDim `#A7B1B6`
- line `#3C464D` · lineStrong `#55626B`
- amber `#E5A22C` (this is THE accent) · teal text `#7FCDD1` / fill `#3E8F94`
- error text `#E68C82` / fill `#C4514A`
- film `#1A1F23` · filmLine `#38424A` · fieldInk `#20262B`

Light (`:root[data-theme="light"]`):
- ground `#DFE1E0` · panel `#ECEEED` · panel2 `#F6F7F6`
- ink `#1B2124` · inkDim `#4A5459`
- line `#B4BAB8` · lineStrong `#8B9492`
- amber text `#7A5208` / fill `#E5A22C` · teal text `#1F6A6E` / fill `#3E8F94`
- error text `#9E3B34` / fill `#C4514A`

**Purple must be gone entirely.** Teal is not a second accent — it is the link/data colour only.

### Typography — system faces only, no new dependencies, no downloaded fonts

The web uses Nimbus Sans Narrow / Bitstream Charter / DejaVu Sans Mono, which do not exist on Android.
Use the platform's real equivalents:

- **display** (masthead, buttons, section labels) → condensed grotesque: `FontFamily(Font("sans-serif-condensed"))`
- **body** → a real serif, not the UI default: `FontFamily.Serif`
- **mono** (percent, speed, ETA, timecode, file size, any number) → `FontFamily.Monospace`, and set
  **tabular figures** (`fontFeatureSettings = "tnum"`) so digits do not jitter while counting.

Keep the web's relative scale (step-0 `1rem` → step-4 `2.6rem`); translate to sensible `sp` values and
keep the same hierarchy. Meta/labels are small, uppercase, and letter-spaced in the display face.

### Rules (from `docs/UI_POLISH.md`, all binding)

- Instrument panel: mono-caps meta, hairline borders, one amber accent, **sharp corners (0dp radius)**.
- Contrast ≥ **4.5:1** for body text and ≥ **3:1** for large text, in **both** themes.
- **No emoji. No em/en dashes in user-facing copy. No `!`.**
- Respect reduced motion — the web honours `prefers-reduced-motion`; on Android gate any animation on
  `ValueAnimator.areAnimatorsEnabled()` so a user with animations off gets no motion.
- The primary action uses the **amber** accent. Do not introduce a third accent (the web review
  explicitly rejected a teal DOWNLOAD button).

## What to change

Work in `app/src/main/`. The existing composables to restyle are `VideoDownloaderTheme`,
`MainScreen`, `SettingsPanel`, `ThemeButton`, `LogPanel` and `LogLine` in
`app/src/main/java/dev/ayaanngandhi/zingy/MainActivity.kt`.

1. **`VideoDownloaderTheme`** — replace both `darkColorScheme(...)` and `lightColorScheme(...)` with the
   palette above, and supply a `Typography` built from the three system families. Put the palette in one
   clearly-named place so it is not re-typed per composable.
2. **Screens** — apply the design: a masthead, hairline-separated panels on the ground colour, sharp
   corners, mono-caps labels, tabular mono for all numbers (progress percent, speed, ETA, counts),
   amber for the one primary action, teal only for links.
3. **`res/values/colors.xml`** — the stock Material `purple_*` / `teal_*` entries are now unused; replace
   them with the palette names you actually use.
4. **`res/values/themes.xml`** — `Theme.Zingy` currently parents `android:Theme.Material.Light.NoActionBar`
   with `statusBarColor = purple_700`. Point it at the ground colour and make the status bar match the
   theme (including `windowLightStatusBar` for light mode) so the system chrome does not clash.
5. **Accessibility** — every interactive control keeps a content description; do not shrink touch
   targets below 48dp while restyling.

Do **not** change `DownloaderViewModel`, `DownloadService`, `SettingsDataStore`, download behaviour,
permissions, or any network/yt-dlp logic. This is a visual change only.

## Constraints

- Only files under `app/src/main/` may change.
- Do **not** add dependencies or download fonts.
- Do **not** run `git add`, `git commit`, `git stash` or `git push`. Leave the tree for review.
- Do **not** use `pkill`/`killall`.
- ⚠️ **Disk is at ~99% with ~2 GB free** on this box and Gradle needs room. If a build fails for disk
  space, say so explicitly and stop — do not thrash retrying.

## Verification — you must actually run these

⚠️ **The APK build is currently BROKEN for a pre-existing reason unrelated to this task. Do NOT try to
fix it and do NOT thrash retrying it.**

`./gradlew assembleDebug` fails at `:app:generateDebugPythonRequirements` because Chaquopy is configured
with `version = "3.8"` in `app/build.gradle.kts`, and modern `yt-dlp` no longer supports Python 3.8. The
requirement `yt-dlp>=2024.12.1` is therefore unsatisfiable — pip only offers up to `2024.10.22` for 3.8
(PyPI itself has 2026.8.19). Raising Chaquopy's Python version is a separate task, out of scope here.

Also: **the root disk is at ~100% (about 900 MB free)** and a Gradle build consumes roughly 2 GB. Do not
run a full build — it can fill the disk and destabilise the host.

So verify what you can, and be honest about what you cannot:

1. **Static review of your own diff.** Re-read every line you changed as a compiler would: every symbol you
   reference must exist and be imported. Report the exact list of imports you used and confirm each one is
   available from libraries already declared in `app/build.gradle.kts`. Flag anything you are unsure about
   instead of assuming it compiles.
2. **No purple remains.** Run and report the output of:
   ```bash
   grep -rniE "6200EE|BB86FC|3700B3|03DAC5|018786|purple" app/src/main/
   ```
   It must return nothing that you cannot justify.
3. **Palette fidelity.** Compare your Compose colours against `webapp/static/zingy.css` value by value and
   state that each hex matches. Any deliberate deviation, name it and say why.
4. **Contrast.** For the text/background pairs you used in each theme, compute the contrast ratios and
   confirm body >= 4.5:1 and large text >= 3:1. Show the numbers.
5. **Behaviour untouched.** Confirm the download flow, settings persistence and theme switching logic are
   unchanged — name anything you had to adjust to keep it working.
6. Do **not** claim the app builds, and do not report any build output you did not get.

## When finished

Write `docs/MOBILE_AESTHETIC_DONE.md`: files changed, the palette/typography actually used, the build
result, the grep result, and the contrast numbers. State plainly anything you could not verify.
