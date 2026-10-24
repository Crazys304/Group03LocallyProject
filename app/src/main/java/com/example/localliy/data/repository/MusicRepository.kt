package com.example.localliy.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.localliy.data.model.Album
import com.example.localliy.data.model.Artist
import com.example.localliy.data.model.Playlist
import com.example.localliy.data.model.PlaylistIconType
import com.example.localliy.data.model.PlaylistThemes
import com.example.localliy.data.model.Song
import com.example.localliy.data.model.SortOption
import org.json.JSONArray
import org.json.JSONObject
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
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA
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
                val dataColumn = c.getColumnIndex(MediaStore.Audio.Media.DATA)

                val scanned = mutableListOf<Song>()
                while (c.moveToNext()) {
                    val id = c.getLong(idColumn)
                    val title = c.getString(titleColumn) ?: "Unknown Track"
                    val artist = c.getString(artistColumn) ?: "Unknown Artist"
                    val album = c.getString(albumColumn) ?: "Unknown Album"
                    val duration = c.getLong(durationColumn)
                    val albumId = c.getLong(albumIdColumn)
                    val dataPath = if (dataColumn >= 0) c.getString(dataColumn) ?: "" else ""

                    // Filter out WhatsApp voice notes & WhatsApp audio files
                    val isWhatsAppAudio = dataPath.contains("WhatsApp", ignoreCase = true) ||
                            dataPath.contains("com.whatsapp", ignoreCase = true) ||
                            artist.contains("WhatsApp", ignoreCase = true) ||
                            album.contains("WhatsApp", ignoreCase = true) ||
                            title.contains("AUD-", ignoreCase = true) ||
                            title.contains("PTT-", ignoreCase = true)

                    if (isWhatsAppAudio) {
                        continue // Exclude WhatsApp audio
                    }

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
            loadPlaylistsFromPrefs(context)
        } catch (_: Exception) {
            // Permission missing or content query error
        }
        return localSongs.size
    }

    fun clearStorageAndReset(context: Context) {
        localSongs.clear()
        customPlaylists.clear()
        try {
            val prefs = context.getSharedPreferences("localliy_playlists", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        } catch (_: Exception) {}
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
        context: Context? = null,
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
        context?.let { savePlaylistsToPrefs(it) }
        return newPlaylist
    }

    fun updatePlaylist(
        context: Context? = null,
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
            context?.let { savePlaylistsToPrefs(it) }
            return true
        }
        return false
    }

    fun deletePlaylist(context: Context? = null, playlistId: String): Boolean {
        val removed = customPlaylists.removeIf { it.id == playlistId }
        if (removed && context != null) {
            savePlaylistsToPrefs(context)
        }
        return removed
    }

    fun addSongsToPlaylist(context: Context? = null, playlistId: String, songsToAdd: List<Song>) {
        val index = customPlaylists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val existing = customPlaylists[index]
            val updatedSongs = (existing.songs + songsToAdd).distinctBy { it.id }
            customPlaylists[index] = existing.copy(songs = updatedSongs)
            context?.let { savePlaylistsToPrefs(it) }
        }
    }

    fun removeSongFromPlaylist(context: Context? = null, playlistId: String, songId: String) {
        val index = customPlaylists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val existing = customPlaylists[index]
            val updatedSongs = existing.songs.filterNot { it.id == songId }
            customPlaylists[index] = existing.copy(songs = updatedSongs)
            context?.let { savePlaylistsToPrefs(it) }
        }
    }

    fun savePlaylistsToPrefs(context: Context) {
        try {
            val prefs = context.getSharedPreferences("localliy_playlists", Context.MODE_PRIVATE)
            val jsonArray = JSONArray()

            customPlaylists.forEach { playlist ->
                val obj = JSONObject()
                obj.put("id", playlist.id)
                obj.put("name", playlist.name)
                obj.put("description", playlist.description)
                obj.put("iconType", playlist.iconType.name)
                obj.put("customImageUri", playlist.customImageUri ?: "")

                val c1 = playlist.gradientColors.getOrNull(0)?.toArgb() ?: 0
                val c2 = playlist.gradientColors.getOrNull(1)?.toArgb() ?: 0
                obj.put("color1", c1)
                obj.put("color2", c2)

                val songIdsArray = JSONArray()
                playlist.songs.forEach { songIdsArray.put(it.id) }
                obj.put("songIds", songIdsArray)

                jsonArray.put(obj)
            }

            prefs.edit().putString("saved_playlists_json", jsonArray.toString()).apply()
        } catch (_: Exception) {
            // Saving failure
        }
    }

    fun loadPlaylistsFromPrefs(context: Context) {
        try {
            val prefs = context.getSharedPreferences("localliy_playlists", Context.MODE_PRIVATE)
            val jsonString = prefs.getString("saved_playlists_json", null) ?: return
            val jsonArray = JSONArray(jsonString)

            customPlaylists.clear()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val description = obj.optString("description", "")
                val iconTypeName = obj.optString("iconType", "MUSIC")
                val customImageUri = obj.optString("customImageUri", "").ifBlank { null }
                val color1 = obj.optInt("color1", 0)
                val color2 = obj.optInt("color2", 0)

                val iconType = try {
                    PlaylistIconType.valueOf(iconTypeName)
                } catch (_: Exception) {
                    PlaylistIconType.MUSIC
                }

                val gradientColors = if (color1 != 0 && color2 != 0) {
                    listOf(Color(color1), Color(color2))
                } else {
                    PlaylistThemes.EmeraldGreen.colors
                }

                val songIdsArray = obj.optJSONArray("songIds") ?: JSONArray()
                val playlistSongIds = mutableSetOf<String>()
                for (j in 0 until songIdsArray.length()) {
                    playlistSongIds.add(songIdsArray.getString(j))
                }

                val playlistSongs = localSongs.filter { playlistSongIds.contains(it.id) }

                customPlaylists.add(
                    Playlist(
                        id = id,
                        name = name,
                        description = description,
                        songs = playlistSongs,
                        iconType = iconType,
                        customImageUri = customImageUri,
                        gradientColors = gradientColors
                    )
                )
            }
        } catch (_: Exception) {
            // Restore failure
        }
    }
}
