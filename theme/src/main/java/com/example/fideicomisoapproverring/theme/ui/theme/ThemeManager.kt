package com.example.fideicomisoapproverring.theme.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Context.dataStore by preferencesDataStore(name = "theme_preferences")

class ThemeManager(private val context: Context) {
    companion object {
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
        private val FOLLOW_SYSTEM = booleanPreferencesKey("follow_system")
        private val USE_AUTO_THEME = booleanPreferencesKey("use_auto_theme")
        private val AUTO_DARK_START = intPreferencesKey("auto_dark_start")
        private val AUTO_DARK_END = intPreferencesKey("auto_dark_end")
        
        // Default times (20:00 - 06:00)
        private const val DEFAULT_DARK_START = 20
        private const val DEFAULT_DARK_END = 6
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val useAutoTheme = preferences[USE_AUTO_THEME] ?: false
        if (useAutoTheme) {
            val currentHour = LocalTime.now().hour
            val darkStart = preferences[AUTO_DARK_START] ?: DEFAULT_DARK_START
            val darkEnd = preferences[AUTO_DARK_END] ?: DEFAULT_DARK_END
            
            if (darkStart > darkEnd) {
                // Night spans across midnight
                currentHour >= darkStart || currentHour < darkEnd
            } else {
                // Night is within same day
                currentHour in darkStart..darkEnd
            }
        } else {
            preferences[IS_DARK_MODE] ?: false
        }
    }

    val useDynamicColors: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_DYNAMIC_COLORS] ?: true
    }

    val followSystem: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FOLLOW_SYSTEM] ?: true
    }

    val useAutoTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_AUTO_THEME] ?: false
    }

    val autoDarkStart: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[AUTO_DARK_START] ?: DEFAULT_DARK_START
    }

    val autoDarkEnd: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[AUTO_DARK_END] ?: DEFAULT_DARK_END
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    suspend fun setUseDynamicColors(useDynamic: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLORS] = useDynamic
        }
    }

    suspend fun setFollowSystem(follow: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FOLLOW_SYSTEM] = follow
        }
    }

    suspend fun setUseAutoTheme(useAuto: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_AUTO_THEME] = useAuto
        }
    }

    suspend fun setAutoDarkHours(startHour: Int, endHour: Int) {
        require(startHour in 0..23) { "Start hour must be between 0 and 23" }
        require(endHour in 0..23) { "End hour must be between 0 and 23" }
        
        context.dataStore.edit { preferences ->
            preferences[AUTO_DARK_START] = startHour
            preferences[AUTO_DARK_END] = endHour
        }
    }
}

@Composable
fun rememberThemeManager(context: Context): ThemeManager {
    return remember { ThemeManager(context) }
} 