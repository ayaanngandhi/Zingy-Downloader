package dev.ayaanngandhi.zingy

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
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

class MainActivity : ComponentActivity() {

    private val viewModel: DownloaderViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Toast.makeText(this, "Storage permission required", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()
        handleSharedIntent(intent)

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            VideoDownloaderTheme(themeSetting = state.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSharedIntent(intent)
    }

    private fun handleSharedIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                // Extract URL from shared text
                val urlPattern = "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)".toRegex()
                val matchResult = urlPattern.find(sharedText)
                matchResult?.value?.let { url ->
                    viewModel.updateUrl(url)
                }
            }
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to general settings if app-specific doesn't work
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        startActivity(intent)
                    } catch (e2: Exception) {
                        Toast.makeText(this, "Please grant storage access in Settings", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            val permissions = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (permissions.isNotEmpty()) {
                permissionLauncher.launch(permissions.toTypedArray())
            }
        }
    }
}

// ---------------------------------------------------------------- palette
//
// One place for every colour in the "Cutting Room" palette. These are the
// exact tokens from webapp/static/zingy.css.

data class ZingyPalette(
    val ground: Color,
    val panel: Color,
    val panel2: Color,
    val ink: Color,
    val inkDim: Color,
    val line: Color,
    val lineStrong: Color,
    val amberText: Color,
    val amberFill: Color,
    val onAmber: Color,
    val tealText: Color,
    val tealFill: Color,
    val errorText: Color,
    val errorFill: Color,
    val film: Color,
    val filmLine: Color,
    val filmInk: Color,
    val filmDim: Color,
    val fieldInk: Color
)

private val DarkZingyPalette = ZingyPalette(
    ground = Color(0xFF20262B),
    panel = Color(0xFF2A3137),
    panel2 = Color(0xFF333C43),
    ink = Color(0xFFECE8E1),
    inkDim = Color(0xFFA7B1B6),
    line = Color(0xFF3C464D),
    lineStrong = Color(0xFF55626B),
    amberText = Color(0xFFE5A22C),
    amberFill = Color(0xFFE5A22C),
    onAmber = Color(0xFF20262B),
    tealText = Color(0xFF7FCDD1),
    tealFill = Color(0xFF3E8F94),
    errorText = Color(0xFFE68C82),
    errorFill = Color(0xFFC4514A),
    film = Color(0xFF1A1F23),
    filmLine = Color(0xFF38424A),
    filmInk = Color(0xFFECE8E1),
    filmDim = Color(0xFFA7B1B6),
    fieldInk = Color(0xFF20262B)
)

private val LightZingyPalette = ZingyPalette(
    ground = Color(0xFFDFE1E0),
    panel = Color(0xFFECEEED),
    panel2 = Color(0xFFF6F7F6),
    ink = Color(0xFF1B2124),
    inkDim = Color(0xFF4A5459),
    line = Color(0xFFB4BAB8),
    lineStrong = Color(0xFF8B9492),
    amberText = Color(0xFF7A5208),
    amberFill = Color(0xFFE5A22C),
    onAmber = Color(0xFF1B2124),
    tealText = Color(0xFF1F6A6E),
    tealFill = Color(0xFF3E8F94),
    errorText = Color(0xFF9E3B34),
    errorFill = Color(0xFFC4514A),
    film = Color(0xFF1A1F23),
    filmLine = Color(0xFF38424A),
    filmInk = Color(0xFFECE8E1),
    filmDim = Color(0xFFA7B1B6),
    fieldInk = Color(0xFF1B2124)
)

private val LocalZingyPalette = staticCompositionLocalOf { DarkZingyPalette }

val MaterialTheme.zingy: ZingyPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalZingyPalette.current

private fun ZingyPalette.toColorScheme(dark: Boolean): ColorScheme =
    if (dark) {
        darkColorScheme(
            primary = amberFill,
            onPrimary = onAmber,
            primaryContainer = panel2,
            onPrimaryContainer = ink,
            secondary = tealFill,
            onSecondary = film,
            secondaryContainer = panel2,
            onSecondaryContainer = tealText,
            tertiary = tealText,
            onTertiary = film,
            tertiaryContainer = panel2,
            onTertiaryContainer = tealText,
            background = ground,
            onBackground = ink,
            surface = panel,
            onSurface = ink,
            surfaceVariant = panel2,
            onSurfaceVariant = inkDim,
            surfaceTint = amberFill,
            inverseSurface = ink,
            inverseOnSurface = ground,
            inversePrimary = amberText,
            error = errorFill,
            onError = Color.White,
            errorContainer = panel2,
            onErrorContainer = errorText,
            outline = lineStrong,
            outlineVariant = line,
            scrim = Color.Black
        )
    } else {
        lightColorScheme(
            primary = amberFill,
            onPrimary = onAmber,
            primaryContainer = panel2,
            onPrimaryContainer = ink,
            secondary = tealFill,
            onSecondary = film,
            secondaryContainer = panel2,
            onSecondaryContainer = tealText,
            tertiary = tealText,
            onTertiary = film,
            tertiaryContainer = panel2,
            onTertiaryContainer = tealText,
            background = ground,
            onBackground = ink,
            surface = panel,
            onSurface = ink,
            surfaceVariant = panel2,
            onSurfaceVariant = inkDim,
            surfaceTint = amberFill,
            inverseSurface = ink,
            inverseOnSurface = ground,
            inversePrimary = amberText,
            error = errorFill,
            onError = Color.White,
            errorContainer = panel2,
            onErrorContainer = errorText,
            outline = lineStrong,
            outlineVariant = line,
            scrim = Color.Black
        )
    }

// ------------------------------------------------------------- typography
//
// Platform faces only: condensed grotesque for display, serif for body,
// monospace with tabular figures for every number.

private val DisplayFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))

private val MonoLarge = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 40.sp,
    lineHeight = 44.sp,
    fontFeatureSettings = "tnum"
)

private val MonoMedium = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 21.sp,
    fontFeatureSettings = "tnum"
)

private val MonoSmall = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    fontFeatureSettings = "tnum"
)

private val ZingyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 34.sp
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.3.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.3.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.4.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.3.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 16.sp,
        lineHeight = 25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 15.sp,
        lineHeight = 23.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp
    )
)

@Composable
fun VideoDownloaderTheme(
    themeSetting: String = SettingsDataStore.THEME_DARK,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeSetting) {
        SettingsDataStore.THEME_LIGHT -> false
        SettingsDataStore.THEME_DARK -> true
        SettingsDataStore.THEME_AUTO -> isSystemInDarkTheme()
        else -> true
    }

    val palette = if (isDarkTheme) DarkZingyPalette else LightZingyPalette
    val view = LocalView.current

    SideEffect {
        applyWindowChrome(view, palette, isDarkTheme)
    }

    CompositionLocalProvider(LocalZingyPalette provides palette) {
        MaterialTheme(
            colorScheme = palette.toColorScheme(isDarkTheme),
            typography = ZingyTypography,
            content = content
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun applyWindowChrome(view: View, palette: ZingyPalette, isDark: Boolean) {
    val window = view.context.findActivity()?.window ?: return
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = palette.ground.toArgb()
    val controller = WindowCompat.getInsetsController(window, view)
    controller.isAppearanceLightStatusBars = !isDark
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        window.navigationBarColor = palette.ground.toArgb()
        controller.isAppearanceLightNavigationBars = !isDark
    }
}

// ------------------------------------------------------------- components

private fun animatorsEnabled(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ValueAnimator.areAnimatorsEnabled()
    } else {
        true
    }

@Composable
private fun ZingyPanel(
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.zingy.panel,
    borderColor: Color = MaterialTheme.zingy.line,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(container)
            .border(1.dp, borderColor)
            .padding(contentPadding),
        content = content
    )
}

@Composable
private fun SectionHead(title: String, ref: String? = null) {
    val p = MaterialTheme.zingy
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = p.ink
            )
            if (ref != null) {
                Text(
                    text = ref.uppercase(),
                    style = MonoSmall,
                    color = p.inkDim,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, bottom = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(p.line)
        )
    }
}

@Composable
private fun ReelMark() {
    val amber = MaterialTheme.zingy.amberText
    Canvas(modifier = Modifier.size(34.dp)) {
        val stroke = 2.2.dp.toPx()
        val radius = size.minDimension / 2f - stroke / 2f
        drawCircle(color = amber, radius = radius, style = Stroke(width = stroke))
        drawCircle(color = amber, radius = size.minDimension * 0.09f, center = center)
        val orbit = size.minDimension * 0.22f
        val marks = listOf(
            Offset(center.x, center.y - orbit),
            Offset(center.x + orbit, center.y),
            Offset(center.x, center.y + orbit),
            Offset(center.x - orbit, center.y)
        )
        marks.forEach { point ->
            drawCircle(
                color = amber,
                radius = size.minDimension * 0.075f,
                center = point
            )
        }
    }
}

@Composable
private fun LiveLamp() {
    val p = MaterialTheme.zingy
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(p.amberFill, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "LIVE",
            style = MonoSmall,
            color = p.inkDim,
            letterSpacing = 1.8.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: DownloaderViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val p = MaterialTheme.zingy
    val motion = animatorsEnabled()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Masthead
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(p.panel)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReelMark()
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ZINGY",
                        style = MaterialTheme.typography.displaySmall,
                        color = p.ink
                    )
                    Text(
                        text = "film transfer bench",
                        style = MaterialTheme.typography.labelSmall,
                        color = p.inkDim,
                        letterSpacing = 1.sp
                    )
                }
                LiveLamp()
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = { viewModel.toggleSettings() }) {
                    Icon(
                        imageVector = if (state.showSettings) Icons.Filled.Close else Icons.Filled.Settings,
                        contentDescription = if (state.showSettings) "Close settings" else "Settings",
                        tint = p.ink
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(p.lineStrong)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Settings panel
            AnimatedVisibility(
                visible = state.showSettings,
                enter = if (motion) expandVertically() + fadeIn() else EnterTransition.None,
                exit = if (motion) shrinkVertically() + fadeOut() else ExitTransition.None
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsPanel(
                        downloadPath = state.downloadPath,
                        theme = state.theme,
                        onDownloadPathChange = { viewModel.updateDownloadPath(it) },
                        onThemeChange = { viewModel.updateTheme(it) },
                        onReset = { viewModel.resetPath() }
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                }
            }

            // Load a source
            SectionHead(title = "Load a source", ref = "reel 01")
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.url,
                onValueChange = { viewModel.updateUrl(it) },
                label = {
                    Text(
                        text = "VIDEO URL",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                placeholder = {
                    Text(
                        text = "https://www.youtube.com/watch?v=...",
                        style = MonoMedium,
                        color = p.inkDim
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
                textStyle = MonoMedium.copy(color = p.ink),
                shape = RectangleShape,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.download()
                    }
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Link,
                        contentDescription = null,
                        tint = p.inkDim
                    )
                },
                trailingIcon = {
                    if (state.url.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateUrl("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = p.inkDim
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = p.ink,
                    unfocusedTextColor = p.ink,
                    disabledTextColor = p.inkDim,
                    focusedContainerColor = p.panel,
                    unfocusedContainerColor = p.panel,
                    disabledContainerColor = p.panel,
                    cursorColor = p.amberText,
                    focusedBorderColor = p.amberText,
                    unfocusedBorderColor = p.lineStrong,
                    disabledBorderColor = p.line,
                    focusedLabelColor = p.amberText,
                    unfocusedLabelColor = p.inkDim,
                    disabledLabelColor = p.inkDim,
                    focusedPlaceholderColor = p.inkDim,
                    unfocusedPlaceholderColor = p.inkDim,
                    focusedLeadingIconColor = p.inkDim,
                    unfocusedLeadingIconColor = p.inkDim,
                    focusedTrailingIconColor = p.inkDim,
                    unfocusedTrailingIconColor = p.inkDim
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Paste a link from any supported source. Zingy reads it, then transfers the file straight to this device.",
                style = MaterialTheme.typography.bodySmall,
                color = p.inkDim
            )

            state.detectedPlatform?.let { platform ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (platform) {
                            "instagram" -> Icons.Filled.CameraAlt
                            "youtube" -> Icons.Filled.PlayCircle
                            "tiktok" -> Icons.Filled.MusicNote
                            "twitter" -> Icons.Filled.Tag
                            "facebook" -> Icons.Filled.Facebook
                            "reddit" -> Icons.Filled.Forum
                            "twitch" -> Icons.Filled.Videocam
                            else -> Icons.Filled.VideoLibrary
                        },
                        contentDescription = null,
                        tint = p.inkDim,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SOURCE: ${platform.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = p.inkDim
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.getText()?.text?.let { text ->
                            viewModel.updateUrl(text)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = RectangleShape,
                    enabled = !state.isLoading,
                    border = BorderStroke(2.dp, p.lineStrong),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = p.panel2,
                        contentColor = p.ink,
                        disabledContainerColor = p.panel2,
                        disabledContentColor = p.inkDim
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentPaste,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PASTE",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.download()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    shape = RectangleShape,
                    enabled = !state.isLoading && state.url.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = p.amberFill,
                        contentColor = p.onAmber,
                        disabledContainerColor = p.amberFill.copy(alpha = 0.55f),
                        disabledContentColor = p.onAmber
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    if (!state.isLoading) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (state.isLoading) "DOWNLOADING" else "DOWNLOAD",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Transfer counter
            AnimatedVisibility(
                visible = state.isLoading,
                enter = if (motion) expandVertically() + fadeIn() else EnterTransition.None,
                exit = if (motion) shrinkVertically() + fadeOut() else ExitTransition.None
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(22.dp))
                    SectionHead(title = "Transfer counter", ref = "reel 02")
                    Spacer(modifier = Modifier.height(12.dp))

                    ZingyPanel {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = state.progress.coerceIn(0, 100).toString().padStart(2, '0'),
                                style = MonoLarge,
                                color = p.ink
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PERCENT",
                                style = MaterialTheme.typography.labelSmall,
                                color = p.inkDim,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(p.line)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(
                                        (state.progress.coerceIn(0, 100) / 100f)
                                    )
                                    .fillMaxHeight()
                                    .background(p.amberFill)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "STATE",
                            style = MaterialTheme.typography.labelSmall,
                            color = p.inkDim
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.status,
                            style = MaterialTheme.typography.bodyMedium,
                            color = p.ink
                        )
                    }
                }
            }

            // Success
            AnimatedVisibility(
                visible = state.successMessage != null,
                enter = if (motion) expandVertically() + fadeIn() else EnterTransition.None,
                exit = if (motion) shrinkVertically() + fadeOut() else ExitTransition.None
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    StatusPanel(
                        text = state.successMessage ?: "",
                        accent = p.tealFill,
                        textColor = p.ink,
                        icon = Icons.Filled.CheckCircle,
                        onDismiss = { viewModel.clearMessages() }
                    )
                }
            }

            // Error
            AnimatedVisibility(
                visible = state.error != null,
                enter = if (motion) expandVertically() + fadeIn() else EnterTransition.None,
                exit = if (motion) shrinkVertically() + fadeOut() else ExitTransition.None
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    StatusPanel(
                        text = state.error ?: "",
                        accent = p.errorFill,
                        textColor = p.errorText,
                        icon = Icons.Filled.Error,
                        onDismiss = { viewModel.clearMessages() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            LogPanel(
                logs = state.logs,
                showLogs = state.showLogs,
                onToggleLogs = { viewModel.toggleLogs() },
                onClearLogs = { viewModel.clearLogs() }
            )

            Spacer(modifier = Modifier.height(22.dp))

            // How to use
            SectionHead(title = "How to use", ref = "reference")
            Spacer(modifier = Modifier.height(12.dp))

            ZingyPanel {
                HowToRow("01", "Copy a video link from any supported source.")
                Spacer(modifier = Modifier.height(10.dp))
                HowToRow("02", "Paste it with the Paste button.")
                Spacer(modifier = Modifier.height(10.dp))
                HowToRow("03", "Tap Download and follow the transfer counter.")
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "YouTube, Instagram, TikTok, Twitter/X, Facebook, Reddit, Twitch, Vimeo and more.",
                    style = MaterialTheme.typography.bodySmall,
                    color = p.inkDim
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Files are written to the download path in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = p.inkDim
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HowToRow(number: String, text: String) {
    val p = MaterialTheme.zingy
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = number,
            style = MonoSmall,
            color = p.amberText,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = p.ink
        )
    }
}

@Composable
private fun StatusPanel(
    text: String,
    accent: Color,
    textColor: Color,
    icon: ImageVector,
    onDismiss: () -> Unit
) {
    val p = MaterialTheme.zingy
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(p.panel)
            .drawBehind {
                drawRect(color = accent, size = Size(6.dp.toPx(), size.height))
            }
            .padding(start = 18.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Dismiss",
                tint = p.inkDim
            )
        }
    }
}

@Composable
fun SettingsPanel(
    downloadPath: String,
    theme: String,
    onDownloadPathChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onReset: () -> Unit
) {
    val p = MaterialTheme.zingy

    // Use local state to avoid cursor jumping
    var localDownloadPath by remember { mutableStateOf(downloadPath) }

    // Sync local state when external state changes (e.g., reset)
    LaunchedEffect(downloadPath) {
        if (localDownloadPath != downloadPath) {
            localDownloadPath = downloadPath
        }
    }

    ZingyPanel {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.labelLarge,
            color = p.ink
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "THEME",
            style = MaterialTheme.typography.labelMedium,
            color = p.inkDim
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeButton(
                label = "Light",
                icon = Icons.Filled.LightMode,
                isSelected = theme == SettingsDataStore.THEME_LIGHT,
                onClick = { onThemeChange(SettingsDataStore.THEME_LIGHT) },
                modifier = Modifier.weight(1f)
            )
            ThemeButton(
                label = "Dark",
                icon = Icons.Filled.DarkMode,
                isSelected = theme == SettingsDataStore.THEME_DARK,
                onClick = { onThemeChange(SettingsDataStore.THEME_DARK) },
                modifier = Modifier.weight(1f)
            )
            ThemeButton(
                label = "Auto",
                icon = Icons.Filled.BrightnessAuto,
                isSelected = theme == SettingsDataStore.THEME_AUTO,
                onClick = { onThemeChange(SettingsDataStore.THEME_AUTO) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "DOWNLOAD PATH",
            style = MaterialTheme.typography.labelMedium,
            color = p.inkDim
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = localDownloadPath,
            onValueChange = { newValue ->
                localDownloadPath = newValue
                onDownloadPathChange(newValue)
            },
            label = {
                Text(
                    text = "SAVE LOCATION",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MonoMedium.copy(color = p.ink),
            shape = RectangleShape,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Folder,
                    contentDescription = null,
                    tint = p.inkDim
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = p.ink,
                unfocusedTextColor = p.ink,
                disabledTextColor = p.inkDim,
                focusedContainerColor = p.panel2,
                unfocusedContainerColor = p.panel2,
                disabledContainerColor = p.panel2,
                cursorColor = p.amberText,
                focusedBorderColor = p.amberText,
                unfocusedBorderColor = p.lineStrong,
                disabledBorderColor = p.line,
                focusedLabelColor = p.amberText,
                unfocusedLabelColor = p.inkDim,
                disabledLabelColor = p.inkDim,
                focusedPlaceholderColor = p.inkDim,
                unfocusedPlaceholderColor = p.inkDim,
                focusedLeadingIconColor = p.inkDim,
                unfocusedLeadingIconColor = p.inkDim
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .align(Alignment.End)
                .heightIn(min = 48.dp),
            shape = RectangleShape,
            border = BorderStroke(2.dp, p.lineStrong),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = p.panel2,
                contentColor = p.ink
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "RESET TO DEFAULT",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun ThemeButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p = MaterialTheme.zingy

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RectangleShape,
        border = BorderStroke(2.dp, if (isSelected) p.amberFill else p.lineStrong),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) p.amberFill else p.panel2,
            contentColor = if (isSelected) p.onAmber else p.ink
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun LogPanel(
    logs: List<LogEntry>,
    showLogs: Boolean,
    onToggleLogs: () -> Unit,
    onClearLogs: () -> Unit
) {
    val p = MaterialTheme.zingy
    val logScrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    var copyConfirm by remember { mutableStateOf(false) }
    val motion = animatorsEnabled()

    // Auto-scroll to bottom when new logs are added
    LaunchedEffect(logs.size) {
        if (motion) {
            logScrollState.animateScrollTo(logScrollState.maxValue)
        } else {
            logScrollState.scrollTo(logScrollState.maxValue)
        }
    }

    // Reset copy confirmation after delay
    LaunchedEffect(copyConfirm) {
        if (copyConfirm) {
            kotlinx.coroutines.delay(2000)
            copyConfirm = false
        }
    }

    ZingyPanel(
        container = p.film,
        borderColor = p.filmLine,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LOG",
                style = MaterialTheme.typography.labelLarge,
                color = p.filmInk
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = logs.size.toString(),
                style = MonoSmall,
                color = p.filmDim
            )
            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = {
                val logText = logs.joinToString("\n") { "[${it.timestamp}] [${it.level}] ${it.message}" }
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(logText))
                copyConfirm = true
            }) {
                Icon(
                    imageVector = if (copyConfirm) Icons.Filled.Check else Icons.Filled.ContentCopy,
                    contentDescription = "Copy logs",
                    tint = if (copyConfirm) p.tealText else p.filmDim,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onClearLogs) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = "Clear logs",
                    tint = p.filmDim,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(onClick = onToggleLogs) {
                Icon(
                    imageVector = if (showLogs) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (showLogs) "Collapse" else "Expand",
                    tint = p.filmDim,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showLogs,
            enter = if (motion) expandVertically() + fadeIn() else EnterTransition.None,
            exit = if (motion) shrinkVertically() + fadeOut() else ExitTransition.None
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 8.dp)
                    .verticalScroll(logScrollState)
                    .border(1.dp, p.filmLine)
                    .padding(8.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        text = "No logs yet.",
                        style = MonoSmall,
                        color = p.filmDim
                    )
                } else {
                    logs.forEach { log ->
                        LogLine(log)
                    }
                }
            }
        }
    }
}

@Composable
fun LogLine(log: LogEntry) {
    val p = MaterialTheme.zingy
    val levelColor = when (log.level) {
        "ERROR" -> p.errorText
        "WARN" -> p.amberText
        "DEBUG" -> p.filmDim
        else -> p.tealText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        Text(
            text = "[${log.timestamp}]",
            style = MonoSmall.copy(fontSize = 10.sp, lineHeight = 14.sp),
            color = p.filmDim
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "[${log.level}]",
            style = MonoSmall.copy(fontSize = 10.sp, lineHeight = 14.sp),
            color = levelColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = log.message,
            style = MonoSmall.copy(fontSize = 10.sp, lineHeight = 14.sp),
            color = p.filmInk,
            modifier = Modifier.weight(1f)
        )
    }
}
