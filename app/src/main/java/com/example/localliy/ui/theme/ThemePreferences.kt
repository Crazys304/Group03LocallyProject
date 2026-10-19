package com.example.localliy.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit

enum class AppThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

enum class AccentColor(
    val displayName: String,
    val previewColor: Color
) {
    GREEN("Green", Color(0xFF22C55E)),
    PURPLE("Purple", Color(0xFF8B5CF6)),
    BLUE("Blue", Color(0xFF3B82F6)),
    TEAL("Teal", Color(0xFF14B8A6)),
    ORANGE("Orange", Color(0xFFF97316)),
    RED("Red", Color(0xFFEF4444))
}

data class ThemeState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.GREEN
)

class ThemePreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)

    var themeMode: AppThemeMode
        get() {
            val name = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
            return runCatching { AppThemeMode.valueOf(name) }.getOrDefault(AppThemeMode.SYSTEM)
        }
        set(value) {
            prefs.edit { putString(KEY_THEME_MODE, value.name) }
        }

    var accentColor: AccentColor
        get() {
            val name = prefs.getString(KEY_ACCENT_COLOR, AccentColor.GREEN.name) ?: AccentColor.GREEN.name
            return runCatching { AccentColor.valueOf(name) }.getOrDefault(AccentColor.GREEN)
        }
        set(value) {
            prefs.edit { putString(KEY_ACCENT_COLOR, value.name) }
        }

    fun getThemeState(): ThemeState {
        return ThemeState(themeMode = themeMode, accentColor = accentColor)
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ACCENT_COLOR = "accent_color"
    }
}
