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

import androidx.compose.runtime.mutableStateListOf

class MusicRepository {

    private var appContext: Context? = null
    private val localSongs = mutableListOf<Song>()
    private val customPlaylists = mutableListOf<Playlist>()
    private val recentlyPlayedSongs = mutableStateListOf<Song>()
    private val recentlyPlayedPlaylists = mutableStateListOf<Playlist>()

    var isPermissionGranted: Boolean = false

    val allSongs: List<Song>
        get() = localSongs

    fun addToRecentlyPlayed(song: Song, context: Context? = null, queueName: String = "Library") {
        context?.let { appContext = it.applicationContext }

        val isPlaylistQueue = queueName == "Liked Songs" || customPlaylists.any { it.name.equals(queueName, ignoreCase = true) }

        if (isPlaylistQueue) {
            val playlist = if (queueName == "Liked Songs") {
                val likedSongs = allSongs.filter { it.isLiked }
                Playlist(
                    id = "liked_songs",
                    name = "Liked Songs",
                    description = "Your favorited tracks",
                    songs = likedSongs,
                    iconType = PlaylistIconType.HEART,
                    gradientColors = listOf(Color(0xFFA855F7), Color(0xFF6B21A8))
                )
            } else {
                customPlaylists.firstOrNull { it.name.equals(queueName, ignoreCase = true) }
            }

            if (playlist != null) {
                recentlyPlayedPlaylists.removeAll { it.id == playlist.id }
                recentlyPlayedPlaylists.add(0, playlist)
                if (recentlyPlayedPlaylists.size > 10) {
                    recentlyPlayedPlaylists.removeAt(recentlyPlayedPlaylists.lastIndex)
                }
            }
        } else {
            recentlyPlayedSongs.removeIf { it.id == song.id }
            recentlyPlayedSongs.add(0, song)
            if (recentlyPlayedSongs.size > 20) {
                recentlyPlayedSongs.removeAt(recentlyPlayedSongs.lastIndex)
            }
        }

        context?.let { saveRecentlyPlayedToPrefs(it) }
    }

    fun getRecentlyPlayedSongs(): List<Song> {
        val unplayed = localSongs.filterNot { song -> recentlyPlayedSongs.any { it.id == song.id } }
        return (recentlyPlayedSongs + unplayed).take(10)
    }

    fun getRecentlyPlayedItems(): List<Any> {
        val playlists = recentlyPlayedPlaylists.toList()
        val songs = localSongs.filterNot { song -> recentlyPlayedSongs.any { it.id == song.id } }
        val recentSongs = recentlyPlayedSongs.toList() + songs

        val combined = mutableListOf<Any>()
        combined.addAll(playlists)
        combined.addAll(recentSongs)
        return combined.take(10)
    }

    fun saveRecentlyPlayedToPrefs(context: Context) {
        appContext = context.applicationContext
        try {
            val prefs = context.getSharedPreferences("localliy_playlists", Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            recentlyPlayedSongs.forEach { jsonArray.put(it.id) }
            prefs.edit().putString("saved_recently_played_json", jsonArray.toString()).apply()
        } catch (_: Exception) {
            // Saving failure
        }
    }

    fun loadRecentlyPlayedFromPrefs(context: Context) {
        appContext = context.applicationContext
        try {
            val prefs = context.getSharedPreferences("localliy_playlists", Context.MODE_PRIVATE)
            val jsonString = prefs.getString("saved_recently_played_json", null) ?: return
            val jsonArray = JSONArray(jsonString)

            recentlyPlayedSongs.clear()
            val songsById = localSongs.associateBy { it.id }
            for (i in 0 until jsonArray.length()) {
                val id = jsonArray.getString(i)
                songsById[id]?.let { song ->
                    recentlyPlayedSongs.add(song)
                }
            }
        } catch (_: Exception) {
            // Loading failure
        }
    }

    fun scanLocalAudio(context: Context): Int {
        appContext = context.applicationContext
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
                    val rawTitle = c.getString(titleColumn) ?: "Unknown Track"
                    val rawArtist = c.getString(artistColumn) ?: "Unknown Artist"
                    val rawAlbum = c.getString(albumColumn) ?: "Unknown Album"
                    val duration = c.getLong(durationColumn)
                    val albumId = c.getLong(albumIdColumn)
                    val dataPath = if (dataColumn >= 0) c.getString(dataColumn) ?: "" else ""

                    // Clean & trim metadata strings
                    val cleanTitle = rawTitle.trim().ifEmpty { "Unknown Track" }
                    val cleanArtist = if (rawArtist.isBlank() || rawArtist.equals("<unknown>", ignoreCase = true)) "Unknown Artist" else rawArtist.trim()
                    val cleanAlbum = if (rawAlbum.isBlank() || rawAlbum.equals("<unknown>", ignoreCase = true)) "Unknown Album" else rawAlbum.trim()

                    // Filter out WhatsApp voice notes & WhatsApp audio files
                    val isWhatsAppAudio = dataPath.contains("WhatsApp", ignoreCase = true) ||
                            dataPath.contains("com.whatsapp", ignoreCase = true) ||
                            cleanArtist.contains("WhatsApp", ignoreCase = true) ||
                            cleanAlbum.contains("WhatsApp", ignoreCase = true) ||
                            cleanTitle.contains("AUD-", ignoreCase = true) ||
                            cleanTitle.contains("PTT-", ignoreCase = true)

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
                            title = cleanTitle,
                            artist = cleanArtist,
                            album = cleanAlbum,
                            durationMs = if (duration <= 0) 180000L else duration,
                            contentUri = contentUri,
                            albumArtUri = albumArtUri
                        )
                    )
                }
                localSongs.clear()
                localSongs.addAll(scanned)
            }
            loadLikedSongsFromPrefs(context)
            loadPlaylistsFromPrefs(context)
            loadRecentlyPlayedFromPrefs(context)
        } catch (_: Exception) {
            // Permission missing or content query error
        }
        return localSongs.size
    }

    fun clearStorageAndReset(context: Context) {
        appContext = context.applicationContext
        localSongs.clear()
        customPlaylists.clear()
        recentlyPlayedSongs.clear()
        recentlyPlayedPlaylists.clear()
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

        // Group songs by normalized album name (trimmed, case-insensitive)
        val grouped = songsList.groupBy { song ->
            song.album.trim().ifEmpty { "Unknown Album" }.lowercase()
        }

        val albums = grouped.map { (_, songs) ->
            val sampleSong = songs.first()
            val albumTitle = sampleSong.album.trim().ifEmpty { "Unknown Album" }

            // Extract distinct artists for this album
            val distinctArtists = songs
                .map { it.artist.trim() }
                .filter { it.isNotBlank() && !it.equals("Unknown Artist", ignoreCase = true) }
                .distinctBy { it.lowercase() }

            val displayArtist = when {
                distinctArtists.size == 1 -> distinctArtists.first()
                distinctArtists.size > 1 -> "Various Artists"
                else -> "Unknown Artist"
            }

            Album(
                name = albumTitle,
                artist = displayArtist,
                songs = songs.sortedBy { it.title.lowercase() },
                albumArtUri = songs.firstOrNull { !it.albumArtUri.isNullOrBlank() }?.albumArtUri
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
        val groupedByArtist = songsList.groupBy { it.artist.trim().lowercase() }
        val albumsList = getAlbums()

        val artists = groupedByArtist.map { (_, artistSongs) ->
            val artistName = artistSongs.first().artist.trim()
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

    fun toggleLike(songId: String, context: Context? = null) {
        context?.let { appContext = it.applicationContext }
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

            appContext?.let { saveLikedSongsToPrefs(it) }
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
        recentlyPlayedSongs.removeIf { it.id == songId }
        appContext?.let {
            saveLikedSongsToPrefs(it)
            saveRecentlyPlayedToPrefs(it)
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
        context?.let { appContext = it.applicationContext }
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
        appContext?.let { savePlaylistsToPrefs(it) }
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
        context?.let { appContext = it.applicationContext }
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
            appContext?.let { savePlaylistsToPrefs(it) }
            return true
        }
        return false
    }

    fun deletePlaylist(context: Context? = null, playlistId: String): Boolean {
        context?.let { appContext = it.applicationContext }
        val removed = customPlaylists.removeIf { it.id == playlistId }
        if (removed && appContext != null) {
            savePlaylistsToPrefs(appContext!!)
        }
        return removed
    }

    fun addSongsToPlaylist(context: Context? = null, playlistId: String, songsToAdd: List<Song>) {
        context?.let { appContext = it.applicationContext }
        val index = customPlaylists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val existing = customPlaylists[index]
            val updatedSongs = (existing.songs + songsToAdd).distinctBy { it.id }
            customPlaylists[index] = existing.copy(songs = updatedSongs)
            appContext?.let { savePlaylistsToPrefs(it) }
        }
    }

    fun removeSongFromPlaylist(context: Context? = null, playlistId: String, songId: String) {
        context?.let { appContext = it.applicationContext }
        val index = customPlaylists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val existing = customPlaylists[index]
            val updatedSongs = existing.songs.filterNot { it.id == songId }
            customPlaylists[index] = existing.copy(songs = updatedSongs)
            appContext?.let { savePlaylistsToPrefs(it) }
        }
    }

    fun saveLikedSongsToPrefs(context: Context) {
        appContext = context.applicationContext
        try {
            val prefs = context.getSharedPreferences("localliy_playlists", Context.MODE_PRIVATE)
            val likedIds = localSongs.filter { it.isLiked }.map { it.id }.toSet()
            prefs.edit().putStringSet("liked_song_ids", likedIds).apply()
        } catch (_: Exception) {}
    }

    fun loadLikedSongsFromPrefs(context: Context) {
        appContext = context.applicationContext
        try {
            val prefs = context.getSharedPreferences("localliy_playlists", Context.MODE_PRIVATE)
            val likedIds = prefs.getStringSet("liked_song_ids", emptySet()) ?: emptySet()
            if (likedIds.isNotEmpty()) {
                localSongs.forEachIndexed { index, song ->
                    if (likedIds.contains(song.id)) {
                        localSongs[index] = song.copy(isLiked = true)
                    }
                }
                customPlaylists.forEachIndexed { i, pl ->
                    val updatedSongs = pl.songs.map { s ->
                        if (likedIds.contains(s.id)) s.copy(isLiked = true) else s
                    }
                    customPlaylists[i] = pl.copy(songs = updatedSongs)
                }
            }
        } catch (_: Exception) {}
    }

    fun savePlaylistsToPrefs(context: Context) {
        appContext = context.applicationContext
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
        appContext = context.applicationContext
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

    fun saveLastPlaybackState(context: Context, song: Song?, queueName: String, queueType: String, queueId: String) {
        appContext = context.applicationContext
        try {
            val prefs = context.getSharedPreferences("localliy_playlists", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("last_played_song_id", song?.id ?: "")
                .putString("last_queue_name", queueName)
                .putString("last_queue_type", queueType)
                .putString("last_queue_id", queueId)
                .apply()
        } catch (_: Exception) {}
    }

    fun getRestoredQueue(context: Context): Pair<List<Song>, String> {
        appContext = context.applicationContext
        try {
            val prefs = context.getSharedPreferences("localliy_playlists", Context.MODE_PRIVATE)
            val queueType = prefs.getString("last_queue_type", "LIBRARY") ?: "LIBRARY"
            val queueId = prefs.getString("last_queue_id", "") ?: ""
            val queueName = prefs.getString("last_queue_name", "Library") ?: "Library"

            val queue = when (queueType) {
                "PLAYLIST" -> {
                    if (queueId == "liked_songs") {
                        allSongs.filter { it.isLiked }
                    } else {
                        customPlaylists.firstOrNull { it.id == queueId }?.songs ?: allSongs
                    }
                }
                "ALBUM" -> {
                    getAlbums().firstOrNull { it.name.equals(queueId, ignoreCase = true) }?.songs ?: allSongs
                }
                "ARTIST" -> {
                    getArtists().firstOrNull { it.name.equals(queueId, ignoreCase = true) }?.songs ?: allSongs
                }
                else -> allSongs
            }
            return Pair(if (queue.isNotEmpty()) queue else allSongs, queueName)
        } catch (_: Exception) {
            return Pair(allSongs, "Library")
        }
    }

    fun getLastPlayedSong(context: Context): Song? {
        appContext = context.applicationContext
        try {
            val prefs = context.getSharedPreferences("localliy_playlists", Context.MODE_PRIVATE)
            val songId = prefs.getString("last_played_song_id", null) ?: return null
            val restoredQueue = getRestoredQueue(context).first
            return restoredQueue.firstOrNull { it.id == songId } ?: localSongs.firstOrNull { it.id == songId }
        } catch (_: Exception) {
            return null
        }
    }
}
