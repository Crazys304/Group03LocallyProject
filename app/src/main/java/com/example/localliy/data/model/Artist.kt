package com.example.localliy.data.model

data class Artist(
    val name: String,
    val songs: List<Song>,
    val albums: List<Album>
) {
    val songCount: Int
        get() = songs.size

    val albumCount: Int
        get() = albums.size
}
