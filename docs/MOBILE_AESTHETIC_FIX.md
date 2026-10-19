# Spec addendum — fix the compilation errors you introduced

Your restyle is good and the palette/verification work was solid, but **the code does not compile**.
You claimed you had inspected the AARs to confirm the symbols; two of those symbol claims were wrong.
This is the corrected, measured result — I ran the compiler myself:

```
$ export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
$ export ANDROID_HOME=/home/mac/Android/Sdk
$ ./gradlew :app:compileDebugKotlin --console=plain

e: MainActivity.kt:299:40 None of the following functions can be called with the arguments supplied:
   public fun Font(fileDescriptor: ParcelFileDescriptor, ...): Font
   public fun Font(familyName: DeviceFontFamilyName, ...): Font
   public fun Font(file: File, ...): Font
   public fun Font(resId: Int, ...): Font
e: MainActivity.kt:681:25 Unresolved reference: RectangleShape
e: MainActivity.kt:783:29 Unresolved reference: RectangleShape
e: MainActivity.kt:814:29 Unresolved reference: RectangleShape
e: MainActivity.kt:1128:21 Unresolved reference: RectangleShape
e: MainActivity.kt:1164:21 Unresolved reference: RectangleShape
e: MainActivity.kt:1198:17 Unresolved reference: RectangleShape
Task :app:compileDebugKotlin FAILED
```

## Fix 1 — line 299, the condensed display font

`Font("sans-serif-condensed")` is invalid: **there is no `Font(String)` overload.** The `familyName`
overload takes a `DeviceFontFamilyName`, which is not appropriate here.

Get the platform family through a `Typeface` instead:

```kotlin
import android.graphics.Typeface

private val CondensedFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))
```

`FontFamily(typeface: Typeface)` is the Android-specific overload in `androidx.compose.ui.text.font`.
Use that value everywhere you currently use the broken constructed family. If you prefer, you may instead
fall back to `FontFamily.SansSerif` — but then say so explicitly in your report, because the condensed
display face is part of the intended look.

## Fix 2 — the six `RectangleShape` references

`RectangleShape` is **not** in `androidx.compose.foundation.shape`. It is a top-level value in
**`androidx.compose.ui.graphics`**:

```kotlin
import androidx.compose.ui.graphics.RectangleShape
```

Remove the wrong import from the `foundation.shape` group and add the correct one. Do not "fix" this by
substituting `RoundedCornerShape(0.dp)` — sharp corners are the design, but use the real symbol.

## Required: prove it compiles

⚠️ **This is now the gate. Do not report success without it.** Run exactly:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/home/mac/Android/Sdk
cd /home/mac/projects/android_downloader
./gradlew :app:compileDebugKotlin --console=plain
```

`:app:compileDebugKotlin` deliberately skips the broken Chaquopy Python step, so it **does** work here.
Iterate on any further errors until it prints `BUILD SUCCESSFUL`. Paste the final lines as real evidence.

If new errors appear, fix them the same way — read the compiler message, correct the symbol, re-run. Do not
guess repeatedly: if a symbol is genuinely unavailable in the declared dependency versions, say so and
substitute a value that is available, and report the substitution.

## Constraints (unchanged)

- Only files under `app/src/main/` may be edited, plus appending to `docs/MOBILE_AESTHETIC_DONE.md`.
- **Do NOT run `assembleDebug`.** The root disk has roughly 690 MB free and a full build needs ~2 GB; it
  would fill the disk. `compileDebugKotlin` only, and if you hit `No space left on device`, stop and report.
- Do not touch `webapp/visit_logs.json` (it is a growing runtime log).
- No new dependencies, no downloaded fonts.
- No `git add` / `commit` / `push`. No `pkill`/`killall`.
- Do not change download, permission, persistence or network behaviour. This is still a visual restyle.

## When it compiles

Append a `## Compile fix` section to `docs/MOBILE_AESTHETIC_DONE.md` containing: the two changes you made,
the exact final compiler output, and an honest note on anything still unverified (rendering on a device is
still unverified — I will do that part).
