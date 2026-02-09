package com.meet.dev.analyzer.data.datastore

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.jthemedetecor.OsThemeDetector
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager.PreferencesKey.DARK_MODE_KEY
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager.PreferencesKey.ONBOARDING_DONE_KEY
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager.PreferencesKey.WINDOW_HEIGHT_KEY
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager.PreferencesKey.WINDOW_POSITION_X_KEY
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager.PreferencesKey.WINDOW_POSITION_Y_KEY
import com.meet.dev.analyzer.data.datastore.AppPreferenceManager.PreferencesKey.WINDOW_WIDTH_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class AppPreferenceManager(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKey {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val ONBOARDING_DONE_KEY = booleanPreferencesKey("onboarding_done")
        val WINDOW_WIDTH_KEY = floatPreferencesKey("window_width")
        val WINDOW_HEIGHT_KEY = floatPreferencesKey("window_height")
        val WINDOW_POSITION_X_KEY = floatPreferencesKey("window_position_x")
        val WINDOW_POSITION_Y_KEY = floatPreferencesKey("window_position_y")
        val CRASH_REPORTING_ENABLED = booleanPreferencesKey("crash_reporting_enabled")
        val IS_LOCAL_LOGS_ENABLED = booleanPreferencesKey("is_local_logs_enabled")
    }

    private val detector = OsThemeDetector.getDetector()


    val isDarkMode = dataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY] ?: detector.isDark
    }

    val isOnboardingDone = dataStore.data.map { prefs ->
        prefs[ONBOARDING_DONE_KEY] ?: false
    }

    val windowWidth = dataStore.data.map { prefs ->
        prefs[WINDOW_WIDTH_KEY]?.dp ?: defaultWindowSize.width
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Main),
        started = SharingStarted.Eagerly,
        initialValue = null
    )
    val windowHeight = dataStore.data.map { prefs ->
        prefs[WINDOW_HEIGHT_KEY]?.dp ?: defaultWindowSize.height
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Main),
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val windowPositionX = dataStore.data.map { prefs ->
        prefs[WINDOW_POSITION_X_KEY]?.dp
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Main),
        started = SharingStarted.Eagerly,
        initialValue = null
    )
    val windowPositionY = dataStore.data.map { prefs ->
        prefs[WINDOW_POSITION_Y_KEY]?.dp
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Main),
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val crashReportingEnabled = dataStore.data.map { prefs ->
        prefs[PreferencesKey.CRASH_REPORTING_ENABLED] ?: true
    }
    val isLocalLogsEnabled = dataStore.data.map { prefs ->
        prefs[PreferencesKey.IS_LOCAL_LOGS_ENABLED] ?: true
    }

    suspend fun saveTheme(isDark: Boolean) =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[DARK_MODE_KEY] = isDark
            }
        }

    suspend fun saveOnboardingDone(isDone: Boolean) =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[ONBOARDING_DONE_KEY] = isDone
            }
        }

    suspend fun saveWindowWidthHeight(width: Float, height: Float) =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[WINDOW_WIDTH_KEY] = width
                preferences[WINDOW_HEIGHT_KEY] = height
            }
        }

    suspend fun saveWindowPosition(position: WindowPosition) =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[WINDOW_POSITION_X_KEY] = position.x.value
                preferences[WINDOW_POSITION_Y_KEY] = position.y.value
            }
        }

    suspend fun saveCrashReportingEnabled(enabled: Boolean) =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[PreferencesKey.CRASH_REPORTING_ENABLED] = enabled
            }
        }

    suspend fun saveLocalLogsEnabled(enabled: Boolean) =
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[PreferencesKey.IS_LOCAL_LOGS_ENABLED] = enabled
            }
        }
}

val defaultWindowSize = DpSize(width = 1024.dp, height = 768.dp)
val defaultWindowPosition = WindowPosition.PlatformDefault
