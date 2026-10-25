package com.example.localliy.data.model

import androidx.compose.ui.graphics.Color

enum class PlaylistIconType {
    HEART,
    MUSIC,
    FIRE,
    STAR,
    HEADPHONES,
    WAVE,
    SPARKLES,
    GYM,
    CAR,
    MOON,
    LEAF
}

data class PlaylistTheme(
    val id: String,
    val name: String,
    val colors: List<Color>
)

object PlaylistThemes {
    val EmeraldGreen = PlaylistTheme("green", "Emerald Green", listOf(Color(0xFF22C55E), Color(0xFF064E3B)))
    val PurpleVibe = PlaylistTheme("purple", "Purple Glow", listOf(Color(0xFFA855F7), Color(0xFF581C87)))
    val DefaultBlue = PlaylistTheme("blue", "Royal Blue", listOf(Color(0xFF3B82F6), Color(0xFF1E3A8A)))
    val SunsetOrange = PlaylistTheme("orange", "Sunset Amber", listOf(Color(0xFFF97316), Color(0xFF7C2D12)))
    val CrimsonRed = PlaylistTheme("red", "Crimson Red", listOf(Color(0xFFEF4444), Color(0xFF7F1D1D)))
    val NeonCyan = PlaylistTheme("cyan", "Neon Cyan", listOf(Color(0xFF06B6D4), Color(0xFF164E63)))
    val DarkSlate = PlaylistTheme("slate", "Midnight Slate", listOf(Color(0xFF475569), Color(0xFF0F172A)))

    val allThemes = listOf(
        EmeraldGreen,
        PurpleVibe,
        DefaultBlue,
        SunsetOrange,
        CrimsonRed,
        NeonCyan,
        DarkSlate
    )
}

data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val songs: List<Song> = emptyList(),
    val iconType: PlaylistIconType = PlaylistIconType.MUSIC,
    val customImageUri: String? = null,
    val gradientColors: List<Color> = PlaylistThemes.EmeraldGreen.colors
) {
    val songCount: Int
        get() = songs.size
}
