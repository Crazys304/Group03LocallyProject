package com.example.localliy.data.model

data class Album(
    val name: String,
    val artist: String,
    val songs: List<Song>,
    val albumArtUri: String? = null
) {
    val songCount: Int
        get() = songs.size
}
