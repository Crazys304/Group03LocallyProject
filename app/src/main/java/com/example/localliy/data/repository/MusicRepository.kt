package com.example.localliy.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import com.example.localliy.data.model.Album
import com.example.localliy.data.model.Artist
import com.example.localliy.data.model.Playlist
import com.example.localliy.data.model.PlaylistIconType
import com.example.localliy.data.model.PlaylistThemes
import com.example.localliy.data.model.Song
import com.example.localliy.data.model.SortOption
import java.util.UUID

class MusicRepository {

    private val localSongs = mutableListOf<Song>()
    private val customPlaylists = mutableListOf<Playlist>()

    var isPermissionGranted: Boolean = false

    val allSongs: List<Song>
        get() = localSongs

    fun scanLocalAudio(context: Context): Int {
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val cursor = context.contentResolver.query(
                queryUri,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )

            cursor?.use { c ->
                val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                val scanned = mutableListOf<Song>()
                while (c.moveToNext()) {
                    val id = c.getLong(idColumn)
                    val title = c.getString(titleColumn) ?: "Unknown Track"
                    val artist = c.getString(artistColumn) ?: "Unknown Artist"
                    val album = c.getString(albumColumn) ?: "Unknown Album"
                    val duration = c.getLong(durationColumn)
                    val albumId = c.getLong(albumIdColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    scanned.add(
                        Song(
                            id = "local_$id",
                            title = title,
                            artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                            album = if (album == "<unknown>") "Unknown Album" else album,
                            durationMs = if (duration <= 0) 180000L else duration,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri
                        )
                    )
                }
                localSongs.clear()
                localSongs.addAll(scanned)
            }
        } catch (_: Exception) {
            // Permission missing or content query error
        }
        return localSongs.size
    }

    fun getSongs(
        sortOption: SortOption = SortOption.TITLE_ASC,
        searchQuery: String = ""
    ): List<Song> {
        val filtered = if (searchQuery.isBlank()) {
            allSongs
        } else {
            allSongs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }
        }

        return when (sortOption) {
            SortOption.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortOption.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortOption.ARTIST -> filtered.sortedWith(
                compareBy<Song> { it.artist.lowercase() }.thenBy { it.title.lowercase() }
            )
            SortOption.ALBUM -> filtered.sortedWith(
                compareBy<Song> { it.album.lowercase() }.thenBy { it.title.lowercase() }
            )
            SortOption.DURATION_DESC -> filtered.sortedByDescending { it.durationMs }
        }
    }

    fun getAlbums(searchQuery: String = ""): List<Album> {
        val songsList = allSongs
        val grouped = songsList.groupBy { Pair(it.album, it.artist) }

        val albums = grouped.map { (key, songs) ->
            Album(
                name = key.first,
                artist = key.second,
                songs = songs.sortedBy { it.title.lowercase() },
                albumArtUri = songs.firstOrNull { it.albumArtUri != null }?.albumArtUri
            )
        }.sortedBy { it.name.lowercase() }

        return if (searchQuery.isBlank()) {
            albums
        } else {
            albums.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    fun getArtists(searchQuery: String = ""): List<Artist> {
        val songsList = allSongs
        val groupedByArtist = songsList.groupBy { it.artist }
        val albumsList = getAlbums()

        val artists = groupedByArtist.map { (artistName, artistSongs) ->
            val artistAlbums = albumsList.filter { it.artist.equals(artistName, ignoreCase = true) }
            Artist(
                name = artistName,
                songs = artistSongs.sortedBy { it.title.lowercase() },
                albums = artistAlbums
            )
        }.sortedBy { it.name.lowercase() }

        return if (searchQuery.isBlank()) {
            artists
        } else {
            artists.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    fun getPlaylists(searchQuery: String = ""): List<Playlist> {
        val likedSongs = allSongs.filter { it.isLiked }

        val likedPlaylist = Playlist(
            id = "liked_songs",
            name = "Liked Songs",
            description = "Your favorited tracks",
            songs = likedSongs,
            iconType = PlaylistIconType.HEART,
            gradientColors = listOf(Color(0xFFA855F7), Color(0xFF6B21A8))
        )

        val totalPlaylists = listOf(likedPlaylist) + customPlaylists

        return if (searchQuery.isBlank()) {
            totalPlaylists
        } else {
            totalPlaylists.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    fun getCustomPlaylists(): List<Playlist> = customPlaylists

    fun toggleLike(songId: String) {
        val localIndex = localSongs.indexOfFirst { it.id == songId }
        if (localIndex != -1) {
            val song = localSongs[localIndex]
            val updatedSong = song.copy(isLiked = !song.isLiked)
            localSongs[localIndex] = updatedSong

            // Update song state inside all custom playlists
            customPlaylists.forEachIndexed { i, pl ->
                if (pl.songs.any { it.id == songId }) {
                    val updatedSongs = pl.songs.map {
                        if (it.id == songId) updatedSong else it
                    }
                    customPlaylists[i] = pl.copy(songs = updatedSongs)
                }
            }
        }
    }

    fun deleteSong(songId: String) {
        localSongs.removeIf { it.id == songId }
        customPlaylists.forEach { pl ->
            val updatedSongs = pl.songs.filterNot { it.id == songId }
            val index = customPlaylists.indexOf(pl)
            if (index != -1) {
                customPlaylists[index] = pl.copy(songs = updatedSongs)
            }
        }
    }

    fun createPlaylist(
        name: String,
        description: String = "",
        iconType: PlaylistIconType = PlaylistIconType.MUSIC,
        gradientColors: List<Color> = PlaylistThemes.EmeraldGreen.colors,
        customImageUri: String? = null,
        initialSongs: List<Song> = emptyList()
    ): Playlist {
        val newPlaylist = Playlist(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            songs = initialSongs,
            iconType = iconType,
            customImageUri = customImageUri,
            gradientColors = gradientColors
        )
        customPlaylists.add(newPlaylist)
        return newPlaylist
    }

    fun updatePlaylist(
        playlistId: String,
        newName: String,
        newDescription: String,
        newIconType: PlaylistIconType,
        newGradientColors: List<Color>,
        newCustomImageUri: String?
    ): Boolean {
        val index = customPlaylists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val existing = customPlaylists[index]
            customPlaylists[index] = existing.copy(
                name = newName,
                description = newDescription,
                iconType = newIconType,
                gradientColors = newGradientColors,
                customImageUri = newCustomImageUri
            )
            return true
        }
        return false
    }

    fun deletePlaylist(playlistId: String): Boolean {
        return customPlaylists.removeIf { it.id == playlistId }
    }

    fun addSongsToPlaylist(playlistId: String, songsToAdd: List<Song>) {
        val index = customPlaylists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val existing = customPlaylists[index]
            val updatedSongs = (existing.songs + songsToAdd).distinctBy { it.id }
            customPlaylists[index] = existing.copy(songs = updatedSongs)
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        val index = customPlaylists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val existing = customPlaylists[index]
            val updatedSongs = existing.songs.filterNot { it.id == songId }
            customPlaylists[index] = existing.copy(songs = updatedSongs)
        }
    }
}
