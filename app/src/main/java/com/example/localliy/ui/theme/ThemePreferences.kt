package com.example.localliy.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit

enum class AppThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

enum class AccentColor(
    val displayName: String,
    val defaultColor: Color
) {
    RED("Red", Color(0xFFEF4444)),
    GREEN("Green", Color(0xFF22C55E)),
    BLUE("Blue", Color(0xFF3B82F6)),
    CUSTOM("Custom", Color(0xFF8B5CF6))
}

data class ThemeState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.GREEN,
    val customAccentColor: Color = Color(0xFF8B5CF6)
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

    var customAccentColor: Color
        get() {
            val colorInt = prefs.getInt(KEY_CUSTOM_ACCENT_COLOR, Color(0xFF8B5CF6).toArgb())
            return Color(colorInt)
        }
        set(value) {
            prefs.edit { putInt(KEY_CUSTOM_ACCENT_COLOR, value.toArgb()) }
        }

    fun getThemeState(): ThemeState {
        return ThemeState(
            themeMode = themeMode,
            accentColor = accentColor,
            customAccentColor = customAccentColor
        )
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_CUSTOM_ACCENT_COLOR = "custom_accent_color"
    }
}
