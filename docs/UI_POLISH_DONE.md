# Zingy UI — polish pass (ADDENDUM B): DONE

Polish only. No redesign, no CSS class renames, no API contract change. The instrument-panel
language is unchanged: mono caps meta, hairlines, one amber accent, sharp corners, both themes.

## Defects fixed

1. **DOWNLOAD off-palette teal** — the button now uses `.btn--amber`, the same treatment as FETCH.
   The now-unused `.btn--teal` rule was removed. One accent on the primary action.
2. **Hazard-tape band read as an error banner** — `.slate__clap` is now a quiet labelled strip,
   `SLATE · READY` in mono caps, on ground with a hairline under it, visually softer than the title.
   No amber tape.
3. **Dead gap right of DOWNLOAD** — `.field--controls .btn { width: 100%; }`, so the button now
   spans the row and lines up flush with the format select.
4. **Dead space under the footer** — `body` is a flex column at `min-height: 100vh` and `.bench`
   flexes to fill, so the footer band runs to the viewport bottom. The page no longer ends in a void.
5. **Native select chevron** — `.format-select` uses `appearance: none` plus a custom chevron
   (inline SVG background), with a light-theme variant. Matches the rest of the dark UI.
6. **Privacy stated twice** — removed the duplicate sentence from the `.hint`; the stronger footer
   note is the single instance.
7. **Unlabelled orange dot** — the pulsing lamp is now grouped and labelled `LIVE` in mono caps.
8. **Mouse-only filmstrip** — added a real Pause/Play control (`#strip-toggle`, `aria-pressed`),
   a `:focus-visible` pause on the focusable viewport, and kept the existing hover pause. The
   `prefers-reduced-motion` block is untouched and still stops the marquee.
9. **Decorative counter** — dropped `15 shown / 1000+`. The pause control now occupies that slot.
10. **Cropped / soft thumbnail** — the frame is a stable `16 / 9` box with `object-fit: cover` and
    top alignment, so the frame is never squeezed and the TITLE row top aligns with the thumbnail.
11. **`film-transfer bench`** — the tagline is now `film transfer bench` (plain spacing, no long
    hyphen) on the index, privacy and terms pages.

## One supporting change outside the visual layer

`get_available_formats()` in `webapp/app.py` now passes the same YouTube `player_client`
(`['android', 'web']`) that the download task already uses. The default extractor was being
served YouTube's "Sign in to confirm you're not a bot" page in this environment, which made the
FETCH step fail and blocked `flowcheck.py`. This aligns formats extraction with the existing
download path; the request/response contract is unchanged.

## Self-checks (run on dev server port 4322)

- `bash /home/mac/clawd-ayaan/zingy-redesign/check.sh` → **GATE_PASS**
  (pixel diff vs baseline 0.87/0.74/0.76/0.69; all leaf text passes contrast in both themes,
  worst 5.39; continuous animations ≥ 3; no console errors).
- `/usr/bin/python3.12 /home/mac/clawd-ayaan/zingy-redesign/flowcheck.py http://127.0.0.1:4322`
  → **FLOWCHECK_PASS** (fetch populates the card, ring fills/hand rotates/pct reaches 100 and
  `saved`, reduced-motion stops continuous animation, placeholder readable).

Neither check script was edited.
