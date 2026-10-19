# Spec — make the APK buildable again (Chaquopy Python version)

## Why this exists

`./gradlew assembleDebug` has been failing at `:app:generateDebugPythonRequirements`:

```
Could not find a version that satisfies the requirement yt-dlp>=2024.12.1
(from versions: ..., 2024.10.22, ...)
```

**Root cause (already diagnosed — do not re-investigate):** `app/build.gradle.kts` sets
`chaquopy { defaultConfig { version = "3.8" } }`. That `version` is the **target Python bundled into the
APK**, not the host interpreter (the host one is `buildPython("/usr/bin/python3.12")`).

Modern `yt-dlp` dropped Python 3.8 support, so for a 3.8 target pip can only see `yt-dlp` up to
`2024.10.22` — the requirement floor of `>=2024.12.1` can never be satisfied. PyPI itself has
`2026.8.19`. This is why the project has been unbuildable.

**Verified:** Chaquopy plugin `15.0.1` (declared in `build.gradle.kts`) supports Python **3.10 and 3.11**.
`3.11` satisfies `yt-dlp>=2024.12.1`.

## Deliverable

### 1. Raise the Chaquopy target Python

In `app/build.gradle.kts`, change:

```kotlin
chaquopy {
    defaultConfig {
        buildPython("/usr/bin/python3.12")
        version = "3.8"          // -> "3.11"
        pip {
            install("yt-dlp>=2024.12.1")
        }
    }
}
```

to `version = "3.11"`. Keep `buildPython` and the `pip` block as they are — do not loosen or pin the
`yt-dlp` requirement unless step 3 forces it.

### 2. Audit the app's own Python for 3.8-isms

The app embeds Python and calls yt-dlp. Find its Python source (look under `app/src/main/python/` or
wherever the Chaquopy source set points — check the `sourceSets` block) and check it for anything that
breaks on 3.11: `asyncio.coroutine`, `@asyncio.coroutine`, removed `collections` ABC aliases
(`from collections import Mapping`), `imp`, `distutils`, `inspect.getargspec`, 3.8-only syntax. Report
what you find, and fix only outright breakages — do not refactor working code.

### 3. Check the app's yt-dlp API usage against the version that will actually install

⚠️ **This is a real risk, report it honestly.** `>=2024.12.1` will resolve to the **latest** release
(2026.8.19) — a jump of nearly two years, because the app has never successfully built with this pin.
yt-dlp's Python API does change.

- Read the app's Python and note exactly how it invokes yt-dlp (module API, `YoutubeDL` options dict,
  `extract_info`, hooks, `postprocessors`, or a subprocess call).
- Compare those call sites against the version pip actually downloads. Check for renamed/removed options
  or changed defaults.
- **Report any incompatibility you find.** If something genuinely does not exist any more, pin
  `yt-dlp` to the newest version that still supports it and say so explicitly with the reason — but do
  **not** pin it reflexively, because a downloader needs a current yt-dlp to work with YouTube at all.
- Do not claim the app works at runtime. It cannot be exercised here.

### 4. Build the APK

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/home/mac/Android/Sdk
cd /home/mac/projects/android_downloader
./gradlew assembleDebug --console=plain
```

Chaquopy will download its Python 3.11 runtime and the yt-dlp wheels on first run — this needs network
and takes a few minutes. That is expected.

## ⚠️ Disk discipline — read this before you start

The root filesystem has roughly **1.1 GB free and is at 99%**. This is the single biggest risk.

- **Before building**, run `du -sh app/build` and remove it (`rm -rf app/build`) if it exists — a stale
  build tree wastes hundreds of MB.
- Sense disk as you go: `df -h /`. If free space drops **below 150 MB, stop the build immediately**
  (`Ctrl-C` / kill the gradle process), run `rm -rf app/build`, and report what happened.
- **Never** free space by deleting anything outside the project: do **not** prune Docker (a Pi-hole
  container is the house's DNS — removing images would take DNS down), do not touch `/mnt/HDD`, do not
  delete `~/.gradle` (the build needs that cache), do not delete `/home/mac/Android/Sdk`.
- Do not run a second gradle build in parallel with this one.

## Constraints

- Only `app/build.gradle.kts` and the app's own Python source may change. Do **not** touch
  `MainActivity.kt` or any Compose/UI file — the restyle is finished and compiles; leave it alone.
- Do not modify `webapp/visit_logs.json`.
- No `git add` / `commit` / `push`. No `pkill`/`killall`.
- Do not install anything on a device — the supervisor will do that.

## Verification — report real output

1. `./gradlew assembleDebug` ends with `BUILD SUCCESSFUL`.
2. `ls -la app/build/outputs/apk/debug/app-debug.apk` — report the exact size.
3. `sha256sum app/build/outputs/apk/debug/app-debug.apk`
4. `df -h /` after the build — report free space.
5. `grep -n 'version = ' app/build.gradle.kts` — show the new value.
6. Your findings from steps 2 and 3 (the Python audit and the yt-dlp API check).

If the build fails, report the **actual** error text and what you tried. Do not claim a build you did not
get, and do not leave the user with no APK and no reason.

## When finished

Append `## APK build` to `docs/MOBILE_AESTHETIC_DONE.md` with the commands, real output, the APK path and
size, and everything still unverified.
