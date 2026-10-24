package com.example.localliy.ui.library

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localliy.data.model.Album
import com.example.localliy.data.model.Artist
import com.example.localliy.data.model.Playlist
import com.example.localliy.data.model.Song
import com.example.localliy.data.model.SortOption
import com.example.localliy.data.repository.MusicRepository
import com.example.localliy.ui.theme.LocalliyTheme

sealed class DetailSelection {
    data class PlaylistDetail(val playlist: Playlist) : DetailSelection()
    data class AlbumDetail(val album: Album) : DetailSelection()
    data class ArtistDetail(val artist: Artist) : DetailSelection()
}

@Composable
fun LibraryScreen(
    repository: MusicRepository,
    onSongSelected: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf(LibraryCategory.PLAYLISTS) }
    var currentSortOption by remember { mutableStateOf(SortOption.TITLE_ASC) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var showSortDialog by remember { mutableStateOf(false) }
    var showCreateOrEditPlaylistDialog by remember { mutableStateOf(false) }
    var editingPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var addingSongsToPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var targetSongForPlaylist by remember { mutableStateOf<Song?>(null) }
    var targetMultipleSongsForPlaylist by remember { mutableStateOf<List<Song>?>(null) }
    var activeDetail by remember { mutableStateOf<DetailSelection?>(null) }

    // Refresh trigger state
    var refreshState by remember { mutableStateOf(0) }

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            repository.isPermissionGranted = true
            repository.scanLocalAudio(context)
            refreshState++
        }
    }

    // Auto scan on load if permissions are granted
    LaunchedEffect(Unit) {
        val scannedCount = repository.scanLocalAudio(context)
        if (scannedCount > 0) {
            repository.isPermissionGranted = true
            refreshState++
        }
    }

    val songs = remember(currentSortOption, searchQuery, refreshState) {
        repository.getSongs(currentSortOption, searchQuery)
    }

    val playlists = remember(searchQuery, refreshState) {
        repository.getPlaylists(searchQuery)
    }

    val albums = remember(searchQuery, refreshState) {
        repository.getAlbums(searchQuery)
    }

    val artists = remember(searchQuery, refreshState) {
        repository.getArtists(searchQuery)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1215))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LibraryHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                isSearchActive = isSearchActive,
                onToggleSearch = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) searchQuery = ""
                },
                onOpenSortDialog = { showSortDialog = true },
                onScanLocalFiles = {
                    permissionLauncher.launch(permissionToRequest)
                },
                onCreatePlaylist = {
                    editingPlaylist = null
                    showCreateOrEditPlaylistDialog = true
                }
            )

            // Storage Permission Scan Banner if not scanned yet
            if (!repository.isPermissionGranted && songs.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E2836))
                        .clickable { permissionLauncher.launch(permissionToRequest) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderSpecial,
                        contentDescription = null,
                        tint = ActiveGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Scan Mobile Device Storage",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to load your local music files into Localliy",
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            CategoryFilterChips(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedCategory) {
                    LibraryCategory.PLAYLISTS -> {
                        PlaylistsSection(
                            playlists = playlists,
                            onPlaylistClick = { activeDetail = DetailSelection.PlaylistDetail(it) },
                            onEditPlaylist = { playlist ->
                                editingPlaylist = playlist
                                showCreateOrEditPlaylistDialog = true
                            },
                            onAddSongsToPlaylist = { playlist ->
                                addingSongsToPlaylist = playlist
                            },
                            onDeletePlaylist = { playlist ->
                                repository.deletePlaylist(playlist.id)
                                refreshState++
                            }
                        )
                    }
                    LibraryCategory.ARTISTS -> {
                        ArtistsSection(
                            artists = artists,
                            onArtistClick = { activeDetail = DetailSelection.ArtistDetail(it) },
                            onPlayArtistSongs = { artist ->
                                if (artist.songs.isNotEmpty()) {
                                    onSongSelected(artist.songs.first())
                                }
                            },
                            onAddArtistToPlaylist = { artist ->
                                targetMultipleSongsForPlaylist = artist.songs
                            }
                        )
                    }
                    LibraryCategory.ALBUMS -> {
                        AlbumsSection(
                            albums = albums,
                            onAlbumClick = { activeDetail = DetailSelection.AlbumDetail(it) },
                            onPlayAlbum = { album ->
                                if (album.songs.isNotEmpty()) {
                                    onSongSelected(album.songs.first())
                                }
                            },
                            onAddAlbumToPlaylist = { album ->
                                targetMultipleSongsForPlaylist = album.songs
                            }
                        )
                    }
                    LibraryCategory.SONGS -> {
                        SongsSection(
                            songs = songs,
                            currentSortOption = currentSortOption,
                            onOpenSortDialog = { showSortDialog = true },
                            onSongClick = onSongSelected,
                            onToggleLike = { song ->
                                repository.toggleLike(song.id)
                                refreshState++
                            },
                            onAddToPlaylist = { song ->
                                targetSongForPlaylist = song
                            },
                            onDeleteSong = { song ->
                                repository.deleteSong(song.id)
                                refreshState++
                            }
                        )
                    }
                }
            }
        }

        // Dialogs & Sheets
        if (showSortDialog) {
            SortDialog(
                currentSortOption = currentSortOption,
                onSortOptionSelected = { option ->
                    currentSortOption = option
                },
                onDismiss = { showSortDialog = false }
            )
        }

        if (showCreateOrEditPlaylistDialog) {
            CreateOrEditPlaylistDialog(
                existingPlaylist = editingPlaylist,
                onSavePlaylist = { name, description, iconType, gradientColors, customImageUri ->
                    if (editingPlaylist == null) {
                        repository.createPlaylist(
                            name = name,
                            description = description,
                            iconType = iconType,
                            gradientColors = gradientColors,
                            customImageUri = customImageUri
                        )
                    } else {
                        repository.updatePlaylist(
                            playlistId = editingPlaylist!!.id,
                            newName = name,
                            newDescription = description,
                            newIconType = iconType,
                            newGradientColors = gradientColors,
                            newCustomImageUri = customImageUri
                        )
                    }
                    refreshState++
                },
                onDismiss = { showCreateOrEditPlaylistDialog = false }
            )
        }

        targetSongForPlaylist?.let { song ->
            SelectPlaylistForSongDialog(
                song = song,
                playlists = playlists,
                onSelectPlaylist = { selectedPl ->
                    repository.addSongsToPlaylist(selectedPl.id, listOf(song))
                    refreshState++
                },
                onCreateNewPlaylist = {
                    editingPlaylist = null
                    showCreateOrEditPlaylistDialog = true
                },
                onDismiss = { targetSongForPlaylist = null }
            )
        }

        targetMultipleSongsForPlaylist?.let { targetSongs ->
            if (targetSongs.isNotEmpty()) {
                SelectPlaylistForSongDialog(
                    song = targetSongs.first(),
                    playlists = playlists,
                    onSelectPlaylist = { selectedPl ->
                        repository.addSongsToPlaylist(selectedPl.id, targetSongs)
                        refreshState++
                    },
                    onCreateNewPlaylist = {
                        editingPlaylist = null
                        showCreateOrEditPlaylistDialog = true
                    },
                    onDismiss = { targetMultipleSongsForPlaylist = null }
                )
            }
        }

        addingSongsToPlaylist?.let { targetPlaylist ->
            AddSongsToPlaylistDialog(
                playlist = targetPlaylist,
                allSongs = repository.getSongs(),
                onAddSongs = { selectedSongs ->
                    repository.addSongsToPlaylist(targetPlaylist.id, selectedSongs)
                    refreshState++
                    // Update active detail sheet if open
                    if (activeDetail is DetailSelection.PlaylistDetail) {
                        val updated = repository.getPlaylists().firstOrNull { it.id == targetPlaylist.id }
                        if (updated != null) {
                            activeDetail = DetailSelection.PlaylistDetail(updated)
                        }
                    }
                },
                onDismiss = { addingSongsToPlaylist = null }
            )
        }

        activeDetail?.let { detail ->
            when (detail) {
                is DetailSelection.PlaylistDetail -> {
                    val currentPl = playlists.firstOrNull { it.id == detail.playlist.id } ?: detail.playlist
                    DetailSheet(
                        title = currentPl.name,
                        subtitle = "${currentPl.songCount} songs" + if (currentPl.description.isNotBlank()) " • ${currentPl.description}" else "",
                        songs = currentPl.songs,
                        onSongClick = { song ->
                            onSongSelected(song)
                            activeDetail = null
                        },
                        onToggleLike = { song ->
                            repository.toggleLike(song.id)
                            refreshState++
                        },
                        onClose = { activeDetail = null },
                        isPlaylist = currentPl.id != "liked_songs",
                        onAddSongsToPlaylist = {
                            addingSongsToPlaylist = currentPl
                        },
                        onRemoveSongFromPlaylist = { song ->
                            repository.removeSongFromPlaylist(currentPl.id, song.id)
                            refreshState++
                            val updated = repository.getPlaylists().firstOrNull { it.id == currentPl.id }
                            if (updated != null) {
                                activeDetail = DetailSelection.PlaylistDetail(updated)
                            }
                        }
                    )
                }
                is DetailSelection.AlbumDetail -> {
                    DetailSheet(
                        title = detail.album.name,
                        subtitle = "Album by ${detail.album.artist} • ${detail.album.songCount} songs",
                        songs = detail.album.songs,
                        onSongClick = { song ->
                            onSongSelected(song)
                            activeDetail = null
                        },
                        onToggleLike = { song ->
                            repository.toggleLike(song.id)
                            refreshState++
                        },
                        onClose = { activeDetail = null }
                    )
                }
                is DetailSelection.ArtistDetail -> {
                    DetailSheet(
                        title = detail.artist.name,
                        subtitle = "${detail.artist.songCount} songs • ${detail.artist.albumCount} albums",
                        songs = detail.artist.songs,
                        onSongClick = { song ->
                            onSongSelected(song)
                            activeDetail = null
                        },
                        onToggleLike = { song ->
                            repository.toggleLike(song.id)
                            refreshState++
                        },
                        onClose = { activeDetail = null }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    LocalliyTheme {
        LibraryScreen(
            repository = remember { MusicRepository() },
            onSongSelected = {}
        )
    }
}
