package com.example.localliy.ui.library

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localliy.data.model.Album
import com.example.localliy.data.model.Artist
import com.example.localliy.data.model.Playlist
import com.example.localliy.data.model.RepeatMode
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
    onPlayAllQueue: (List<Song>, String) -> Unit = { _, _ -> },
    isShuffleEnabled: Boolean = false,
    repeatMode: RepeatMode = RepeatMode.OFF,
    onToggleShuffle: () -> Unit = {},
    onToggleRepeat: () -> Unit = {},
    onSongLikedToggle: (Song) -> Unit = {},
    onBottomBarVisibilityChanged: (Boolean) -> Unit = {},
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

    // Detect if music-to-playlist selection screen is active
    val isMusicToPlaylistSelectionOpen = addingSongsToPlaylist != null || targetSongForPlaylist != null || targetMultipleSongsForPlaylist != null

    // Layer 3 Back Handler: Close selection screens/dialogs without saving
    BackHandler(enabled = isMusicToPlaylistSelectionOpen || showCreateOrEditPlaylistDialog || showSortDialog) {
        addingSongsToPlaylist = null
        targetSongForPlaylist = null
        targetMultipleSongsForPlaylist = null
        showCreateOrEditPlaylistDialog = false
        showSortDialog = false
    }

    // Layer 2 Back Handler: Close playlist/album/artist detail view back to main library
    BackHandler(enabled = activeDetail != null && !isMusicToPlaylistSelectionOpen && !showCreateOrEditPlaylistDialog && !showSortDialog) {
        activeDetail = null
    }

    // Layer 1 Back Handler: Close active search bar
    BackHandler(enabled = isSearchActive && activeDetail == null && !isMusicToPlaylistSelectionOpen && !showCreateOrEditPlaylistDialog && !showSortDialog) {
        isSearchActive = false
        searchQuery = ""
    }

    LaunchedEffect(isMusicToPlaylistSelectionOpen) {
        onBottomBarVisibilityChanged(!isMusicToPlaylistSelectionOpen)
    }

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
            repository.rescanLocalAudio(context)
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
            .background(MaterialTheme.colorScheme.background)
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
                }
            )

            // Storage Permission Scan Banner if not scanned yet
            if (!repository.isPermissionGranted && songs.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
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
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to load your local music files into Localliy",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                repository.deletePlaylist(context, playlist.id)
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
                                    onPlayAllQueue(artist.songs, artist.name)
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
                                    onPlayAllQueue(album.songs, album.name)
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
                            onSongClick = { song ->
                                onPlayAllQueue(songs, "Songs")
                                onSongSelected(song)
                            },
                            onToggleLike = { song ->
                                repository.toggleLike(song.id, context)
                                onSongLikedToggle(song)
                                refreshState++
                            },
                            onAddToPlaylist = { song ->
                                targetSongForPlaylist = song
                            },
                            onDeleteSong = { song ->
                                repository.deleteSong(song.id, context)
                                refreshState++
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button for creating playlist in the playlist section
        if (selectedCategory == LibraryCategory.PLAYLISTS && activeDetail == null && !isMusicToPlaylistSelectionOpen && !showCreateOrEditPlaylistDialog && !showSortDialog && !isSearchActive) {
            FloatingActionButton(
                onClick = {
                    editingPlaylist = null
                    showCreateOrEditPlaylistDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Playlist",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Detail Sheet (Playlist folder screen / Album / Artist view)
        activeDetail?.let { detail ->
            when (detail) {
                is DetailSelection.PlaylistDetail -> {
                    val currentPl = playlists.firstOrNull { it.id == detail.playlist.id } ?: detail.playlist
                    DetailSheet(
                        title = currentPl.name,
                        subtitle = "${currentPl.songCount} songs" + if (currentPl.description.isNotBlank()) " • ${currentPl.description}" else "",
                        songs = currentPl.songs,
                        isShuffleEnabled = isShuffleEnabled,
                        repeatMode = repeatMode,
                        onToggleShuffle = onToggleShuffle,
                        onToggleRepeat = onToggleRepeat,
                        onPlayAllQueue = onPlayAllQueue,
                        onSongClick = { song ->
                            onPlayAllQueue(currentPl.songs, currentPl.name)
                            onSongSelected(song)
                        },
                        onToggleLike = { song ->
                            repository.toggleLike(song.id, context)
                            onSongLikedToggle(song)
                            refreshState++
                        },
                        onClose = { activeDetail = null },
                        isPlaylist = currentPl.id != "liked_songs",
                        onAddSongsToPlaylist = {
                            addingSongsToPlaylist = currentPl
                        },
                        onRemoveSongFromPlaylist = { song ->
                            repository.removeSongFromPlaylist(context, currentPl.id, song.id)
                            refreshState++
                            val updated = repository.getPlaylists().firstOrNull { it.id == currentPl.id }
                            if (updated != null) {
                                activeDetail = DetailSelection.PlaylistDetail(updated)
                            }
                        }
                    )
                }
                is DetailSelection.AlbumDetail -> {
                    val currentAlbum = albums.firstOrNull { it.name == detail.album.name } ?: detail.album
                    DetailSheet(
                        title = currentAlbum.name,
                        subtitle = "Album by ${currentAlbum.artist} • ${currentAlbum.songCount} songs",
                        songs = currentAlbum.songs,
                        isShuffleEnabled = isShuffleEnabled,
                        repeatMode = repeatMode,
                        onToggleShuffle = onToggleShuffle,
                        onToggleRepeat = onToggleRepeat,
                        onPlayAllQueue = onPlayAllQueue,
                        onSongClick = { song ->
                            onPlayAllQueue(currentAlbum.songs, currentAlbum.name)
                            onSongSelected(song)
                        },
                        onToggleLike = { song ->
                            repository.toggleLike(song.id, context)
                            onSongLikedToggle(song)
                            refreshState++
                        },
                        onClose = { activeDetail = null }
                    )
                }
                is DetailSelection.ArtistDetail -> {
                    val currentArtist = artists.firstOrNull { it.name == detail.artist.name } ?: detail.artist
                    DetailSheet(
                        title = currentArtist.name,
                        subtitle = "${currentArtist.songCount} songs • ${currentArtist.albumCount} albums",
                        songs = currentArtist.songs,
                        isShuffleEnabled = isShuffleEnabled,
                        repeatMode = repeatMode,
                        onToggleShuffle = onToggleShuffle,
                        onToggleRepeat = onToggleRepeat,
                        onPlayAllQueue = onPlayAllQueue,
                        onSongClick = { song ->
                            onPlayAllQueue(currentArtist.songs, currentArtist.name)
                            onSongSelected(song)
                        },
                        onToggleLike = { song ->
                            repository.toggleLike(song.id, context)
                            onSongLikedToggle(song)
                            refreshState++
                        },
                        onClose = { activeDetail = null }
                    )
                }
            }
        }

        if (showSortDialog) {
            SortDialog(
                currentSortOption = currentSortOption,
                onSortOptionSelected = { option ->
                    currentSortOption = option
                    showSortDialog = false
                },
                onDismiss = { showSortDialog = false }
            )
        }

        if (showCreateOrEditPlaylistDialog) {
            CreateOrEditPlaylistDialog(
                existingPlaylist = editingPlaylist,
                onSavePlaylist = { name, description, iconType, gradientColors, customImageUri ->
                    if (editingPlaylist != null) {
                        repository.updatePlaylist(
                            context = context,
                            playlistId = editingPlaylist!!.id,
                            newName = name,
                            newDescription = description,
                            newIconType = iconType,
                            newGradientColors = gradientColors,
                            newCustomImageUri = customImageUri
                        )
                    } else {
                        repository.createPlaylist(
                            context = context,
                            name = name,
                            description = description,
                            iconType = iconType,
                            gradientColors = gradientColors,
                            customImageUri = customImageUri
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
                    repository.addSongsToPlaylist(context, selectedPl.id, listOf(song))
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
                        repository.addSongsToPlaylist(context, selectedPl.id, targetSongs)
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
                    repository.addSongsToPlaylist(context, targetPlaylist.id, selectedSongs)
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
