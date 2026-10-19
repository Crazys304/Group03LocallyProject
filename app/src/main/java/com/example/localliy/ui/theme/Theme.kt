package com.example.localliy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun getCustomColorScheme(accentColor: AccentColor, darkTheme: Boolean): ColorScheme {
    return if (darkTheme) {
        when (accentColor) {
            AccentColor.GREEN -> darkColorScheme(
                primary = Color(0xFF22C55E),
                onPrimary = Color(0xFF003915),
                primaryContainer = Color(0xFF14532D),
                onPrimaryContainer = Color(0xFF86EFAC),
                secondary = Color(0xFF4ADE80),
                onSecondary = Color(0xFF052E16),
                tertiary = Color(0xFF34D399),
                background = Color(0xFF0F1215),
                onBackground = Color(0xFFF1F5F9),
                surface = Color(0xFF161B22),
                onSurface = Color(0xFFF1F5F9),
                surfaceVariant = Color(0xFF21262D),
                onSurfaceVariant = Color(0xFF94A3B8),
                outline = Color(0xFF334155),
                outlineVariant = Color(0xFF1E293B)
            )
            AccentColor.PURPLE -> darkColorScheme(
                primary = Color(0xFFA855F7),
                onPrimary = Color(0xFF3B0764),
                primaryContainer = Color(0xFF581C87),
                onPrimaryContainer = Color(0xFFE9D5FF),
                secondary = Color(0xFFC084FC),
                onSecondary = Color(0xFF2E1065),
                tertiary = Color(0xFFE879F9),
                background = Color(0xFF0F1215),
                onBackground = Color(0xFFF1F5F9),
                surface = Color(0xFF161B22),
                onSurface = Color(0xFFF1F5F9),
                surfaceVariant = Color(0xFF21262D),
                onSurfaceVariant = Color(0xFF94A3B8),
                outline = Color(0xFF334155),
                outlineVariant = Color(0xFF1E293B)
            )
            AccentColor.BLUE -> darkColorScheme(
                primary = Color(0xFF3B82F6),
                onPrimary = Color(0xFF1E3A8A),
                primaryContainer = Color(0xFF1E40AF),
                onPrimaryContainer = Color(0xFFBFDBFE),
                secondary = Color(0xFF60A5FA),
                onSecondary = Color(0xFF172554),
                tertiary = Color(0xFF38BDF8),
                background = Color(0xFF0F1215),
                onBackground = Color(0xFFF1F5F9),
                surface = Color(0xFF161B22),
                onSurface = Color(0xFFF1F5F9),
                surfaceVariant = Color(0xFF21262D),
                onSurfaceVariant = Color(0xFF94A3B8),
                outline = Color(0xFF334155),
                outlineVariant = Color(0xFF1E293B)
            )
            AccentColor.TEAL -> darkColorScheme(
                primary = Color(0xFF14B8A6),
                onPrimary = Color(0xFF042F2E),
                primaryContainer = Color(0xFF115E59),
                onPrimaryContainer = Color(0xFF99F6E4),
                secondary = Color(0xFF2DD4BF),
                onSecondary = Color(0xFF022C22),
                tertiary = Color(0xFF5EEAD4),
                background = Color(0xFF0F1215),
                onBackground = Color(0xFFF1F5F9),
                surface = Color(0xFF161B22),
                onSurface = Color(0xFFF1F5F9),
                surfaceVariant = Color(0xFF21262D),
                onSurfaceVariant = Color(0xFF94A3B8),
                outline = Color(0xFF334155),
                outlineVariant = Color(0xFF1E293B)
            )
            AccentColor.ORANGE -> darkColorScheme(
                primary = Color(0xFFF97316),
                onPrimary = Color(0xFF431407),
                primaryContainer = Color(0xFF7C2D12),
                onPrimaryContainer = Color(0xFFFFEDD5),
                secondary = Color(0xFFFB923C),
                onSecondary = Color(0xFF270A00),
                tertiary = Color(0xFFFDBA74),
                background = Color(0xFF0F1215),
                onBackground = Color(0xFFF1F5F9),
                surface = Color(0xFF161B22),
                onSurface = Color(0xFFF1F5F9),
                surfaceVariant = Color(0xFF21262D),
                onSurfaceVariant = Color(0xFF94A3B8),
                outline = Color(0xFF334155),
                outlineVariant = Color(0xFF1E293B)
            )
            AccentColor.RED -> darkColorScheme(
                primary = Color(0xFFEF4444),
                onPrimary = Color(0xFF450A0A),
                primaryContainer = Color(0xFF7F1D1D),
                onPrimaryContainer = Color(0xFFFEE2E2),
                secondary = Color(0xFFF87171),
                onSecondary = Color(0xFF2A0808),
                tertiary = Color(0xFFFCA5A5),
                background = Color(0xFF0F1215),
                onBackground = Color(0xFFF1F5F9),
                surface = Color(0xFF161B22),
                onSurface = Color(0xFFF1F5F9),
                surfaceVariant = Color(0xFF21262D),
                onSurfaceVariant = Color(0xFF94A3B8),
                outline = Color(0xFF334155),
                outlineVariant = Color(0xFF1E293B)
            )
        }
    } else {
        when (accentColor) {
            AccentColor.GREEN -> lightColorScheme(
                primary = Color(0xFF16A34A),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFDCFCE7),
                onPrimaryContainer = Color(0xFF14532D),
                secondary = Color(0xFF22C55E),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFF059669),
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF0F172A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF475569),
                outline = Color(0xFFCBD5E1),
                outlineVariant = Color(0xFFE2E8F0)
            )
            AccentColor.PURPLE -> lightColorScheme(
                primary = Color(0xFF7C3AED),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFF3E8FF),
                onPrimaryContainer = Color(0xFF581C87),
                secondary = Color(0xFF8B5CF6),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFFC084FC),
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF0F172A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF475569),
                outline = Color(0xFFCBD5E1),
                outlineVariant = Color(0xFFE2E8F0)
            )
            AccentColor.BLUE -> lightColorScheme(
                primary = Color(0xFF2563EB),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFDBEAFE),
                onPrimaryContainer = Color(0xFF1E40AF),
                secondary = Color(0xFF3B82F6),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFF0284C7),
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF0F172A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF475569),
                outline = Color(0xFFCBD5E1),
                outlineVariant = Color(0xFFE2E8F0)
            )
            AccentColor.TEAL -> lightColorScheme(
                primary = Color(0xFF0D9488),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFCCFBF1),
                onPrimaryContainer = Color(0xFF115E59),
                secondary = Color(0xFF14B8A6),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFF0F766E),
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF0F172A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF475569),
                outline = Color(0xFFCBD5E1),
                outlineVariant = Color(0xFFE2E8F0)
            )
            AccentColor.ORANGE -> lightColorScheme(
                primary = Color(0xFFEA580C),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFFEDD5),
                onPrimaryContainer = Color(0xFF7C2D12),
                secondary = Color(0xFFF97316),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFFC2410C),
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF0F172A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF475569),
                outline = Color(0xFFCBD5E1),
                outlineVariant = Color(0xFFE2E8F0)
            )
            AccentColor.RED -> lightColorScheme(
                primary = Color(0xFFDC2626),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFEE2E2),
                onPrimaryContainer = Color(0xFF7F1D1D),
                secondary = Color(0xFFEF4444),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFFB91C1C),
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
}

@Composable
fun LocalliyTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    accentColor: AccentColor = AccentColor.GREEN,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = getCustomColorScheme(accentColor, darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
