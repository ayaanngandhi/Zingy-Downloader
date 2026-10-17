# Zingy favicon — DONE

The tab mark now matches the on-page film reel. One file changed: `webapp/static/favicon.svg`
(468 bytes). The old purple `#6200EE` Material play button is gone.

## What it is

A self-contained SVG on a `0 0 32 32` viewBox:

- rounded-square dark ground `#20262B` (`rx=7`) so it reads on light *and* dark browser chrome
- one amber `#E5A22C` path, `fill-rule="evenodd"`, holding the reel as a bold filled form:
  - outer disc, `r=13`
  - centre hub rendered as a dark cut-out, `r=3`
  - four perforation cut-outs, `r=2.6`, at the compass points `(16,8.2)`, `(23.8,16)`,
    `(16,23.8)`, `(8.2,16)`

No strokes, no text, no raster, no scripts, no `#6200EE`.

## Rasterised and looked at

Rendered with the project rasteriser (headless Chrome, the same renderer a tab uses), which
writes `fav-<size>.png` plus nearest-neighbour zooms and contact sheets:

```
16 / 32 / 48 / 180 px — all rendered with content
```

**What the 16x16 render actually looks like.** A rounded dark square (corners nipped, about a
2px margin on every side). Inside, a centred round amber disc roughly 12px across. Cut out of
it: a dark centre hole about 2px, and four dark holes about 2px at north, east, south and west,
each sitting between the hub and the rim with a couple of amber pixels of web around them. The
amber reads as a ring-and-web frame around the five holes, not as a blob, and the gaps between
the holes stay open at this size. It is recognisably the same reel as the header mark, and the
disc sits well clear of the tile edge. Silhouette, hole placement and margins all hold.

## Evidence

```
XML: valid
E5A22C count: 1
6200EE count: 0
20262B count: 1
468 webapp/static/favicon.svg      (single file, under 2 KB)
```

Opaque colours in the raster are exactly `#20262B` and `#E5A22C` (top two by pixel count at
16 / 32 / 48 px); other values are antialiasing at the hole edges only.

## Gate

```
bash /home/mac/clawd-ayaan/zingy-redesign/check.sh
...
GATE_PASS
```

The favicon is not on the page, so all nine checks (pixel-diff vs baseline, per-node contrast in
both themes, continuous animations, no console errors) are unaffected, as expected. The first
attempt timed out because CDP on `:9222` was saturated; it attached cleanly on retry.

FAVICON_DONE
