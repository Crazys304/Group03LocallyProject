package com.example.localliy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun isLightColor(color: Color): Boolean {
    val luminance = (0.2126f * color.red) + (0.7152f * color.green) + (0.0722f * color.blue)
    return luminance > 0.6f
}

fun buildColorScheme(primaryColor: Color, darkTheme: Boolean): ColorScheme {
    val onPrimaryColor = if (isLightColor(primaryColor)) Color.Black else Color.White

    return if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryColor.copy(alpha = 0.3f),
            onPrimaryContainer = Color(0xFFF1F5F9),
            secondary = primaryColor,
            onSecondary = onPrimaryColor,
            tertiary = primaryColor,
            background = Color(0xFF0F1215),
            onBackground = Color(0xFFF1F5F9),
            surface = Color(0xFF161B22),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF21262D),
            onSurfaceVariant = Color(0xFF94A3B8),
            outline = Color(0xFF334155),
            outlineVariant = Color(0xFF1E293B)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = onPrimaryColor,
            primaryContainer = primaryColor.copy(alpha = 0.15f),
            onPrimaryContainer = primaryColor,
            secondary = primaryColor,
            onSecondary = onPrimaryColor,
            tertiary = primaryColor,
            background = Color(0xFFF8FAFC),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFFCBD5E1),
            outlineVariant = Color(0xFFE2E8F0)
        )
    }
}

fun getCustomColorScheme(
    accentColor: AccentColor,
    customColor: Color,
    darkTheme: Boolean
): ColorScheme {
    val primaryColor = when (accentColor) {
        AccentColor.RED -> AccentColor.RED.defaultColor
        AccentColor.GREEN -> AccentColor.GREEN.defaultColor
        AccentColor.BLUE -> AccentColor.BLUE.defaultColor
        AccentColor.CUSTOM -> customColor
    }
    return buildColorScheme(primaryColor, darkTheme)
}

@Composable
fun LocalliyTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    accentColor: AccentColor = AccentColor.GREEN,
    customAccentColor: Color = Color(0xFF8B5CF6),
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = getCustomColorScheme(accentColor, customAccentColor, darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
