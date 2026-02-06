package dev.ayaanngandhi.zingy

import android.app.Application
import android.media.MediaScannerConnection
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class LogEntry(
    val timestamp: String,
    val message: String,
    val level: String = "INFO"
)

data class DownloadState(
    val url: String = "",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val status: String = "",
    val error: String? = null,
    val successMessage: String? = null,
    val detectedPlatform: String? = null,
    val downloadPath: String = SettingsDataStore.DEFAULT_DOWNLOAD_PATH,
    val theme: String = SettingsDataStore.THEME_DARK,
    val showSettings: Boolean = false,
    val logs: List<LogEntry> = emptyList(),
    val showLogs: Boolean = true
)

class DownloaderViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val _state = MutableStateFlow(DownloadState())
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    init {
        // Initialize Python
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(application))
        }

        // Load saved path and theme
        viewModelScope.launch {
            settingsDataStore.downloadPath.collect { path ->
                _state.update { it.copy(downloadPath = path) }
            }
        }
        viewModelScope.launch {
            settingsDataStore.theme.collect { theme ->
                _state.update { it.copy(theme = theme) }
            }
        }

        addLog("App initialized", "INFO")
    }

    private fun addLog(message: String, level: String = "INFO") {
        val timestamp = dateFormat.format(Date())
        val entry = LogEntry(timestamp, message, level)
        _state.update { currentState ->
            val newLogs = currentState.logs + entry
            // Keep only last 100 logs
            currentState.copy(logs = newLogs.takeLast(100))
        }
    }

    fun clearLogs() {
        _state.update { it.copy(logs = emptyList()) }
        addLog("Logs cleared", "INFO")
    }

    fun toggleLogs() {
        _state.update { it.copy(showLogs = !it.showLogs) }
    }

    fun updateUrl(url: String) {
        _state.update {
            it.copy(
                url = url,
                error = null,
                successMessage = null,
                detectedPlatform = detectPlatform(url)
            )
        }
    }

    fun toggleSettings() {
        _state.update { it.copy(showSettings = !it.showSettings) }
    }

    fun updateDownloadPath(path: String) {
        viewModelScope.launch {
            settingsDataStore.setDownloadPath(path)
            _state.update { it.copy(downloadPath = path) }
        }
    }

    fun resetPath() {
        viewModelScope.launch {
            settingsDataStore.resetToDefaults()
            _state.update {
                it.copy(downloadPath = SettingsDataStore.DEFAULT_DOWNLOAD_PATH)
            }
        }
        addLog("Path reset to default", "INFO")
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            settingsDataStore.setTheme(theme)
            _state.update { it.copy(theme = theme) }
        }
        addLog("Theme changed to: $theme", "INFO")
    }

    private fun detectPlatform(url: String): String? {
        if (url.isBlank()) return null
        val urlLower = url.lowercase()
        return when {
            urlLower.contains("instagram.com") || urlLower.contains("instagr.am") -> "instagram"
            urlLower.contains("youtube.com") || urlLower.contains("youtu.be") -> "youtube"
            urlLower.contains("tiktok.com") -> "tiktok"
            urlLower.contains("twitter.com") || urlLower.contains("x.com") -> "twitter"
            urlLower.contains("facebook.com") || urlLower.contains("fb.watch") -> "facebook"
            urlLower.contains("vimeo.com") -> "vimeo"
            urlLower.contains("reddit.com") -> "reddit"
            urlLower.contains("twitch.tv") -> "twitch"
            urlLower.contains("dailymotion.com") -> "dailymotion"
            urlLower.contains("soundcloud.com") -> "soundcloud"
            urlLower.matches(Regex("https?://.*")) -> "video" // Any valid URL
            else -> null
        }
    }

    fun download() {
        val url = _state.value.url.trim()
        if (url.isEmpty()) {
            _state.update { it.copy(error = "Please enter a URL") }
            addLog("Error: Empty URL", "ERROR")
            return
        }

        val platform = detectPlatform(url)
        if (platform == null) {
            _state.update { it.copy(error = "Please enter a valid URL") }
            addLog("Error: Invalid URL: $url", "ERROR")
            return
        }

        val outputDir = _state.value.downloadPath

        addLog("Starting download...", "INFO")
        addLog("URL: $url", "INFO")
        addLog("Platform: $platform", "INFO")
        addLog("Output directory: $outputDir", "INFO")

        // Create directory if it doesn't exist
        try {
            val dir = File(outputDir)
            if (!dir.exists()) {
                val created = dir.mkdirs()
                addLog("Directory created: $created", if (created) "INFO" else "WARN")
            }
            if (dir.exists() && dir.canWrite()) {
                addLog("Directory is writable", "INFO")
            } else {
                addLog("Directory may not be writable!", "WARN")
            }
        } catch (e: Exception) {
            addLog("Error checking directory: ${e.message}", "ERROR")
        }

        _state.update {
            it.copy(
                isLoading = true,
                progress = 0,
                status = "Starting download...",
                error = null,
                successMessage = null
            )
        }

        // Start foreground service to keep download running in background
        DownloadService.start(getApplication(), "Downloading...")

        viewModelScope.launch {
            try {
                addLog("Calling Python downloader...", "INFO")

                val result = withContext(Dispatchers.IO) {
                    val py = Python.getInstance()
                    val module = py.getModule("downloader")

                    // Call download_video function
                    val resultJson = module.callAttr(
                        "download_video",
                        url,
                        outputDir,
                        null // Progress callback (simplified for now)
                    ).toString()

                    JSONObject(resultJson)
                }

                // Parse logs from Python
                if (result.has("logs")) {
                    try {
                        val logsArray = result.getJSONArray("logs")
                        for (i in 0 until logsArray.length()) {
                            val logLine = logsArray.getString(i)
                            // Parse [timestamp] [level] message format
                            val match = Regex("\\[([^]]+)] \\[([^]]+)] (.+)").find(logLine)
                            if (match != null) {
                                val (_, level, message) = match.destructured
                                addLog(message, level)
                            } else {
                                addLog(logLine, "INFO")
                            }
                        }
                    } catch (e: Exception) {
                        addLog("Error parsing logs: ${e.message}", "WARN")
                    }
                }

                if (result.getBoolean("success")) {
                    val filePath = result.getString("filename")
                    val filename = File(filePath).name
                    val fileSize = if (result.has("file_size")) {
                        val bytes = result.getLong("file_size")
                        String.format("%.2f MB", bytes / 1024.0 / 1024.0)
                    } else ""

                    addLog("Download successful: $filename ($fileSize)", "INFO")
                    addLog("Full path: $filePath", "INFO")

                    // Trigger media scan so file shows in gallery/file manager
                    addLog("Triggering media scan...", "INFO")
                    MediaScannerConnection.scanFile(
                        getApplication(),
                        arrayOf(filePath),
                        arrayOf("video/mp4")
                    ) { path, uri ->
                        addLog("Media scan complete: $uri", "INFO")
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            progress = 100,
                            status = "Complete",
                            successMessage = "Saved to: $filePath",
                            url = ""
                        )
                    }
                    // Stop foreground service
                    DownloadService.stop(getApplication())
                } else {
                    val errorMsg = result.optString("error", "Download failed")
                    addLog("Download failed: $errorMsg", "ERROR")

                    if (result.has("traceback")) {
                        addLog("Traceback: ${result.getString("traceback")}", "ERROR")
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            progress = 0,
                            status = "",
                            error = errorMsg
                        )
                    }
                    // Stop foreground service
                    DownloadService.stop(getApplication())
                }
            } catch (e: Exception) {
                addLog("Exception: ${e.message}", "ERROR")
                addLog("Stack trace: ${e.stackTraceToString()}", "ERROR")

                _state.update {
                    it.copy(
                        isLoading = false,
                        progress = 0,
                        status = "",
                        error = e.message ?: "An error occurred"
                    )
                }
                // Stop foreground service
                DownloadService.stop(getApplication())
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}
