# Mobile aesthetic pass — done

Date: 2026-10-19

The Android app now uses the "Cutting Room" design language from
`webapp/static/zingy.css`. The work is a visual restyle only. No download,
permission, persistence, network or yt-dlp logic was changed.

## Files changed

Under `app/src/main/`:

- `java/dev/ayaanngandhi/zingy/MainActivity.kt` — palette, typography, theme,
  masthead, panels, transfer counter, settings, log panel, launcher chrome.
- `res/values/colors.xml` — replaced the stock purple/teal entries with the
  light palette tokens.
- `res/values-night/colors.xml` — new; the dark palette tokens.
- `res/values/themes.xml` — `Theme.Zingy` now uses the ground colour, the
  ground window background and `windowLightStatusBar=true`.
- `res/values-night/themes.xml` — new; dark variant of the same theme with
  `windowLightStatusBar=false`.
- `res/drawable/ic_launcher.xml` — recoloured from `#6200EE` to amber and
  field ink so no purple remains anywhere in `app/src/main/`.

Outside `app/src/main/`, only this report was added, as requested:

- `docs/MOBILE_AESTHETIC_DONE.md`

`DownloaderViewModel.kt`, `DownloadService.kt`, `SettingsDataStore.kt`,
`app/build.gradle.kts`, the manifest and the Python source were not edited.

## Palette actually used

All values are the exact tokens from `zingy.css`. A script compared every
Kotlin colour literal against the CSS custom properties and every one matched.

| Token | Dark | Light |
| --- | --- | --- |
| ground | `#20262B` | `#DFE1E0` |
| panel | `#2A3137` | `#ECEEED` |
| panel2 | `#333C43` | `#F6F7F6` |
| ink | `#ECE8E1` | `#1B2124` |
| inkDim | `#A7B1B6` | `#4A5459` |
| line | `#3C464D` | `#B4BAB8` |
| lineStrong | `#55626B` | `#8B9492` |
| amberText | `#E5A22C` | `#7A5208` |
| amberFill | `#E5A22C` | `#E5A22C` |
| tealText | `#7FCDD1` | `#1F6A6E` |
| tealFill | `#3E8F94` | `#3E8F94` |
| errorText | `#E68C82` | `#9E3B34` |
| errorFill | `#C4514A` | `#C4514A` |
| film | `#1A1F23` | `#1A1F23` |
| filmLine | `#38424A` | `#38424A` |
| filmInk | `#ECE8E1` | `#ECE8E1` |
| filmDim | `#A7B1B6` | `#A7B1B6` |
| fieldInk | `#20262B` | `#1B2124` |

Amber is the only accent. Teal is used only for the success rule, INFO log
level and the copy-confirmed icon. Error colours are semantic, not a third
accent. The platform-brand icon colours (YouTube red, Instagram pink, etc.)
were removed in favour of `inkDim`.

Deliberate deviation: `ColorScheme.onError` is `Color.White`, because the CSS
does not define ink for text placed on the error fill. White on `#C4514A` is
4.54:1; the theme's own error banner uses `errorText` on `panel` instead.

## Typography

Platform faces only, no fonts downloaded and no dependencies added:

- display: `FontFamily(Font("sans-serif-condensed"))`
- body: `FontFamily.Serif`
- numbers: `FontFamily.Monospace` with `fontFeatureSettings = "tnum"`

`Typography` slots: display 26–40sp, headlines 18–24sp, titles 14–20sp, body
13–16sp, labels 11–14sp with 1–1.5sp letter spacing. Meta text is rendered in
uppercase. Mono/tabular is used for the progress percentage, log count,
timestamps and every other number currently exposed by the app state. The
Android state does not expose speed or ETA, so those were not invented.

## Screen treatment

- Masthead: reel mark, ZINGY wordmark, "film transfer bench" tagline, LIVE
  lamp, settings control, 3dp hairline under the bar.
- Sections: display-caps heading with a mono `REEL 01` reference and a
  hairline rule.
- Panels: sharp rectangles, 1dp `line` borders, panel fills on the ground.
- Input and path fields: sharp corners, mono text, non-tabular monospace only
  where it is prose, amber focused border/cursor, mono-caps labels.
- Primary action: amber fill, field-ink label. Paste and reset are bordered
  neutral controls. No teal buttons.
- Transfer counter: mono percent readout and a static amber bar. The
  indeterminate spinner and Material progress animation were removed, which
  also removes motion for reduced-motion users.
- Success/error: 6dp left rule in teal/error fill with body text; dismiss is a
  full-size icon button.
- Logs: film-black panel, mono lines, tabular timestamps, semantic level
  colours (ERROR error text, WARN amber, INFO teal, DEBUG dim).
- Touch targets: every button is at least 48dp high; the log header icon
  buttons no longer use 32dp boxes.

## Reduced motion

`ValueAnimator.areAnimatorsEnabled()` gates the `AnimatedVisibility` enter and
exit transitions and the log auto-scroll (`scrollTo` instead of
`animateScrollTo` when disabled). There is no decorative reel spin, lamp pulse
or indeterminate spinner. On API 24–25 the check returns true because the API
does not exist.

## Build result

**Not run, by instruction and because the build is known broken.**

- `./gradlew assembleDebug` fails at `:app:generateDebugPythonRequirements`
  because Chaquopy is pinned to Python 3.8 and `yt-dlp>=2024.12.1` cannot be
  resolved for it. That is pre-existing and out of scope.
- The root filesystem had about 875 MB free when checked (`df -h /`), and a
  Gradle build needs roughly 2 GB. Running it risked filling the disk.
- Consequently no compile, lint, resource-link or APK result is claimed. The
  app was not installed or visually inspected on a device or emulator.

## Static verification performed

### 1. Purple check

```
$ grep -rniE "6200EE|BB86FC|3700B3|03DAC5|018786|purple" app/src/main/
$ echo $?
1
```

No output. Purple is gone, including the launcher icon.

### 2. Palette fidelity

A script extracted the `:root[data-theme="dark"]` and `:root[data-theme="light"]`
blocks from `zingy.css` and the `DarkZingyPalette` / `LightZingyPalette`
literals from `MainActivity.kt`. All 18 mapped tokens matched in both themes,
including `--field-ink` → `onAmber`/`fieldInk`.

### 3. Contrast ratios (WCAG, computed)

Dark theme:

| Pair | Ratio | Minimum |
| --- | --- | --- |
| ink `#ECE8E1` on ground `#20262B` | 12.52:1 | 4.5 |
| ink on panel `#2A3137` | 10.80:1 | 4.5 |
| inkDim `#A7B1B6` on panel | 6.03:1 | 4.5 |
| amberText `#E5A22C` on panel | 5.99:1 | 4.5 |
| tealText `#7FCDD1` on panel | 7.25:1 | 4.5 |
| errorText `#E68C82` on panel | 5.30:1 | 4.5 |
| button label `#20262B` on amber `#E5A22C` | 6.94:1 | 4.5 |
| filmInk `#ECE8E1` on film `#1A1F23` | 13.60:1 | 4.5 |
| filmDim `#A7B1B6` on film | 7.60:1 | 4.5 |

Light theme:

| Pair | Ratio | Minimum |
| --- | --- | --- |
| ink `#1B2124` on ground `#DFE1E0` | 12.39:1 | 4.5 |
| ink on panel `#ECEEED` | 13.97:1 | 4.5 |
| inkDim `#4A5459` on panel | 6.67:1 | 4.5 |
| amberText `#7A5208` on panel | 5.93:1 | 4.5 |
| tealText `#1F6A6E` on panel | 5.39:1 | 4.5 |
| errorText `#9E3B34` on panel | 5.76:1 | 4.5 |
| button label `#1B2124` on amber `#E5A22C` | 7.39:1 | 4.5 |
| filmInk on film | 13.60:1 | 4.5 |
| filmDim on film | 7.60:1 | 4.5 |

The lowest body pair is light-theme teal text on the ground at 4.78:1. All
large display text is far above 3:1.

### 4. Import / symbol review

Exact import list in `MainActivity.kt`:

```kotlin
import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RectangleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
```

Availability:

- `android.*` — Android platform (compileSdk 34, minSdk 24).
- `androidx.compose.animation.*`, `androidx.compose.foundation.*`,
  `androidx.compose.runtime.*`, `androidx.compose.ui.*` — Compose BOM
  `2024.02.00`, already declared. The animation artifact was already in use by
  the previous file.
- `androidx.compose.material3.*` — declared `material3`.
- `androidx.compose.material.icons.*` — declared `material-icons-extended`.
- `androidx.core.content.ContextCompat`, `androidx.core.view.WindowCompat` —
  declared `androidx.core:core-ktx:1.12.0`.
- `androidx.activity.*` — declared `activity-compose:1.8.1`.
- `androidx.lifecycle.compose.*` — declared `lifecycle-runtime-compose:2.6.2`.

I also inspected the AARs already in the Gradle cache to confirm the exact
symbols, since no build was run:

- Compose UI 1.6.1: `Font(deviceFontFamilyName: String, ...)` exists;
  `TextStyle` accepts `fontFeatureSettings`; `EnterTransition.None` and
  `ExitTransition.None` exist.
- Material3 1.2.0: the `ColorScheme` parameters used exist; `OutlinedButton`
  accepts a `border`; `OutlinedTextFieldDefaults.colors` has the label,
  container, icon, placeholder and border parameters used; `Typography` has
  every slot used.
- core 1.12.0: `WindowCompat.getInsetsController`,
  `isAppearanceLightStatusBars` and `isAppearanceLightNavigationBars` exist.

A simple import scan found no unused import. Braces, parentheses and brackets
in the Kotlin file balance. All XML files under `app/src/main/res/` parse.

### 5. Behaviour untouched

- The `MainActivity` permission, shared-intent and storage code is byte-for-byte
  the original apart from imports.
- `DownloaderViewModel`, `DownloadService`, `SettingsDataStore`, download,
  media-scan, clipboard and theme persistence behaviour were not edited.
- The same `SettingsDataStore` theme constants drive `VideoDownloaderTheme`;
  only the colour scheme, typography and status/navigation bar chrome changed.
  The settings path's local-state sync and reset path are unchanged.
- Download, Paste, Clear, Reset, settings, log copy/clear/expand and message
  dismiss actions call the same ViewModel APIs as before.
- The only functional-feeling change is visual: the loading spinner was removed
  and replaced by the "DOWNLOADING" label plus the transfer counter.

## What I could not verify

- Kotlin compilation, Compose compiler output, resource linking (aapt2), lint
  and packaging. No Gradle task was run.
- Rendering on a device or emulator, font fallback for
  `sans-serif-condensed`, status-bar appearance, TalkBack output and actual
  touch behaviour.
- The known Chaquopy / yt-dlp build failure. It is untouched and will still
  block `assembleDebug` until the Python version is raised separately.

## Compile fix

Two symbol errors were fixed:

1. `Font("sans-serif-condensed")` at line 299. `Font(String)` does not exist.
   The display family now uses the Android `Typeface` overload:

   ```kotlin
   import android.graphics.Typeface

   private val DisplayFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))
   ```

   The unused `androidx.compose.ui.text.font.Font` import was removed. The
   condensed display face is retained.

2. The six `RectangleShape` references were unresolved from
   `androidx.compose.foundation.shape`. The import was removed and replaced
   with the correct top-level symbol:

   ```kotlin
   import androidx.compose.ui.graphics.RectangleShape
   ```

   No `RoundedCornerShape(0.dp)` substitution was used.

Verification command (exactly as required, no `assembleDebug`):

```
$ export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
$ export ANDROID_HOME=/home/mac/Android/Sdk
$ ./gradlew :app:compileDebugKotlin --console=plain
```

Real final output:

```
> Task :app:compileDebugKotlin
w: file:///home/mac/projects/android_downloader/app/src/main/java/dev/ayaanngandhi/zingy/DownloaderViewModel.kt:254:25 Parameter 'path' is never used, could be renamed to _

BUILD SUCCESSFUL in 24s
14 actionable tasks: 2 executed, 12 up-to-date
```

The warning is pre-existing and comes from untouched `DownloaderViewModel.kt`.

Still unverified: rendering on a device or emulator, actual font fallback,
status-bar appearance and TalkBack behaviour. `assembleDebug` was not run;
the APK was not built or installed.

## APK build

### Change

`app/build.gradle.kts` now targets Python 3.11:

```kotlin
chaquopy {
    defaultConfig {
        buildPython("/usr/bin/python3.12")
        version = "3.11"
        pip {
            install("yt-dlp>=2024.12.1")
        }
    }
}
```

`buildPython` and the `yt-dlp` requirement were left unchanged. A stale
`app/build` was removed before building. No Compose or UI file was touched.

### Python 3.11 audit

The app has one embedded Python file: `app/src/main/python/downloader.py`.
It contains no `asyncio.coroutine`, no removed `collections` ABC aliases, no
`imp`, no `distutils`, no `inspect.getargspec`, and no Python 3.8-only
syntax. It uses f-strings, `os.makedirs(exist_ok=True)`, context managers and
standard-library modules only, all valid on 3.11. No Python change was needed.

### yt-dlp API check

`pip` installed `yt-dlp 2026.8.19` (the latest release, from
`yt-dlp>=2024.12.1`). The app's Python API usage was checked against that
version:

- All option keys used in the `ydl_opts` dict still exist: `format`,
  `outtmpl`, `progress_hooks`, `quiet`, `verbose`, `no_warnings`,
  `noplaylist`, `merge_output_format`, `socket_timeout`, `retries`,
  `restrictfilenames`, `windowsfilenames`, `noprogress`, `consoletitle`,
  `overwrites`, `extractor_args`, and `prefer_free_formats`.
- `YoutubeDL` as a context manager, `extract_info(download=True)`,
  `prepare_filename`, `requested_downloads`, `filepath`, and
  `yt_dlp.version.__version__` all still exist.
- YouTube `player_client: ['android', 'web']` is still supported. `android`
  is present in `INNERTUBE_CLIENTS` and `web` remains a base client.
- The Instagram `extractor_args` key `skip: ['dash']` is no longer read by
  the Instagram extractor, so it is now an ignored no-op rather than an
  error. It was not changed and no version pin was added, because no
  app-critical API was removed and a current yt-dlp is required for YouTube.

Verification commands:

```
$ rm -rf app/build
$ export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
$ export ANDROID_HOME=/home/mac/Android/Sdk
$ ./gradlew assembleDebug --console=plain
```

Real output excerpt, including the install and final result:

```
> Configure project :app
Warning: Python version 3.11 may have fewer packages available. If you experience problems, try switching to version 3.8.

> Task :app:generateDebugPythonRequirements
Chaquopy: Installing for arm64-v8a
Looking in indexes: https://pypi.org/simple, https://chaquo.com/pypi-13.1
Collecting yt-dlp>=2024.12.1
  Downloading yt_dlp-2026.8.19-py3-none-any.whl (3.2 MB)
Installing collected packages: yt-dlp
Successfully installed yt-dlp-2026.8.19
Chaquopy: Installing for armeabi-v7a
Chaquopy: Installing for x86
Chaquopy: Installing for x86_64
Warning: Failed to compile to .pyc format: buildPython version 3.12.3 is incompatible. See https://chaquo.com/chaquopy/doc/current/android.html#android-bytecode.

> Task :app:compileDebugKotlin
w: file:///home/mac/projects/android_downloader/app/src/main/java/dev/ayaanngandhi/zingy/DownloaderViewModel.kt:254:25 Parameter 'path' is never used, could be renamed to _

> Task :app:packageDebug
> Task :app:createDebugApkListingFileRedirect
> Task :app:assembleDebug

BUILD SUCCESSFUL in 4m 28s
44 actionable tasks: 44 executed
```

The Python 3.11 warning is Chaquopy's generic notice. The `.pyc` warning
means Chaquopy packaged Python source instead of bytecode because the host
build Python is 3.12.3 and the target is 3.11; it did not fail the build.

Artifact:

```
app/build/outputs/apk/debug/app-debug.apk
73,894,963 bytes
sha256 d52f5279f6ab3ba2132faa3beaaf2db8ca2c1e204b89ac995ec0b9bc6e6de316
```

Disk after the build: `475M` free. Disk was checked during the build and
never fell below the 150 MB stop threshold.

Still unverified: installing the APK on a device, download extraction for
YouTube and Instagram, the ignored Instagram `skip` argument at runtime,
startup behavior without `.pyc` files, and all UI rendering. The APK was
built but not installed or run.
