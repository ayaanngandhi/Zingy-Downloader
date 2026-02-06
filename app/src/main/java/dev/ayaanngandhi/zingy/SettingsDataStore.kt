package dev.ayaanngandhi.zingy

import android.content.Context
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val DOWNLOAD_PATH_KEY = stringPreferencesKey("download_path")
        private val THEME_KEY = stringPreferencesKey("theme")

        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_AUTO = "auto"

        val DEFAULT_DOWNLOAD_PATH: String = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "Zingy"
        ).absolutePath
    }

    val downloadPath: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DOWNLOAD_PATH_KEY] ?: DEFAULT_DOWNLOAD_PATH
    }

    val theme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: THEME_DARK
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun setDownloadPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[DOWNLOAD_PATH_KEY] = path
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.remove(DOWNLOAD_PATH_KEY)
        }
    }
}
