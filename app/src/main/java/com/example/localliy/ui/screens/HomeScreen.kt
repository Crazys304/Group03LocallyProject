package com.example.localliy.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.localliy.R
import com.example.localliy.data.model.Playlist
import com.example.localliy.data.model.PlaylistIconType
import com.example.localliy.data.model.RepeatMode
import com.example.localliy.data.model.Song
import com.example.localliy.data.repository.MusicRepository
import com.example.localliy.ui.library.AddSongsToPlaylistDialog
import com.example.localliy.ui.library.DetailSheet

// Preset gradient colors for cards when album art is unavailable
private val CardGradients = listOf(
    listOf(Color(0xFF334636), Color(0xFF19231B)),
    listOf(Color(0xFF3F4638), Color(0xFF1E241B)),
    listOf(Color(0xFF2B3A44), Color(0xFF161E23)),
    listOf(Color(0xFF4A3428), Color(0xFF241812)),
    listOf(Color(0xFF3D2B48), Color(0xFF1F1526))
)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    repository: MusicRepository,
    isShuffleEnabled: Boolean = false,
    repeatMode: RepeatMode = RepeatMode.OFF,
    onToggleShuffle: () -> Unit = {},
    onToggleRepeat: () -> Unit = {},
    onSongSelected: (Song) -> Unit = {},
    onSongLikedToggle: (Song) -> Unit = {},
    onPlayAllQueue: (List<Song>, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var addingSongsToPlaylist by remember { mutableStateOf<Playlist?>(null) }

    // Intercept Back button when Playlist Detail or Add Songs dialog is open on Home Screen
    BackHandler(enabled = selectedPlaylist != null || addingSongsToPlaylist != null) {
        if (addingSongsToPlaylist != null) {
            addingSongsToPlaylist = null
        } else {
            selectedPlaylist = null
        }
    }

    // Real-time filtering based on search query
    val allRecentlyPlayed = repository.getRecentlyPlayedSongs()
    val recentlyPlayed = if (searchQuery.isBlank()) {
        allRecentlyPlayed
    } else {
        allRecentlyPlayed.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    val allPlaylists = repository.getPlaylists()
    val playlists = if (searchQuery.isBlank()) {
        allPlaylists
    } else {
        allPlaylists.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // --- Header ---
            Text(
                text = "Good evening",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ready for some music?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Working Interactive Search Bar ---
            SearchBarUI(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Recently Played Section ---
            Text(
                text = "Recently played",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (recentlyPlayed.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(recentlyPlayed) { song ->
                        val index = recentlyPlayed.indexOf(song)
                        val gradient = CardGradients[index % CardGradients.size]
                        RecentlyPlayedSongCard(
                            song = song,
                            gradientColors = gradient,
                            onClick = {
                                repository.addToRecentlyPlayed(song, context)
                                onSongSelected(song)
                            }
                        )
                    }
                }
            } else {
                Text(
                    text = if (searchQuery.isNotBlank()) "No songs match your search" else "No songs found on device yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // --- Smart Shuffle Banner ---
            SmartShuffleCard(
                onClick = {
                    val allSongs = repository.allSongs
                    if (allSongs.isNotEmpty()) {
                        onPlayAllQueue(allSongs.shuffled(), "Smart Shuffle")
                    }
                }
            )

            Spacer(modifier = Modifier.height(26.dp))

            // --- Your Playlists Section ---
            Text(
                text = "Your playlists",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (playlists.isNotEmpty()) {
                PlaylistsContainer(
                    playlists = playlists,
                    onPlaylistClick = { playlist ->
                        selectedPlaylist = playlist
                    }
                )
            } else {
                Text(
                    text = if (searchQuery.isNotBlank()) "No playlists match your search" else "No playlists created yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // --- Playlist Detail View overlay on Home Screen ---
        selectedPlaylist?.let { activePlaylist ->
            val freshPlaylist = repository.getPlaylists().firstOrNull { it.id == activePlaylist.id } ?: activePlaylist

            DetailSheet(
                title = freshPlaylist.name,
                subtitle = "${freshPlaylist.songs.size} songs" + if (freshPlaylist.description.isNotBlank()) " · ${freshPlaylist.description}" else "",
                songs = freshPlaylist.songs,
                onSongClick = { song ->
                    repository.addToRecentlyPlayed(song, context)
                    onSongSelected(song)
                },
                onPlayAllQueue = onPlayAllQueue,
                onToggleLike = { song ->
                    repository.toggleLike(song.id)
                    onSongLikedToggle(song)
                },
                onClose = { selectedPlaylist = null },
                isPlaylist = true,
                isShuffleEnabled = isShuffleEnabled,
                repeatMode = repeatMode,
                onToggleShuffle = onToggleShuffle,
                onToggleRepeat = onToggleRepeat,
                onAddSongsToPlaylist = if (freshPlaylist.id != "liked_songs") {
                    { addingSongsToPlaylist = freshPlaylist }
                } else null,
                onRemoveSongFromPlaylist = if (freshPlaylist.id != "liked_songs") {
                    { song -> repository.removeSongFromPlaylist(context, freshPlaylist.id, song.id) }
                } else null
            )
        }

        // --- Add Songs to Playlist Dialog overlay ---
        addingSongsToPlaylist?.let { targetPlaylist ->
            AddSongsToPlaylistDialog(
                playlist = targetPlaylist,
                allSongs = repository.allSongs,
                onAddSongs = { selectedSongs ->
                    repository.addSongsToPlaylist(context, targetPlaylist.id, selectedSongs)
                    addingSongsToPlaylist = null
                },
                onDismiss = { addingSongsToPlaylist = null }
            )
        }
    }
}

// --- Interactive Search Bar UI ---
@Composable
private fun SearchBarUI(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (searchQuery.isEmpty()) {
                Text(
                    text = "Search your music, playlists...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            }
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (searchQuery.isNotEmpty()) {
            IconButton(
                onClick = { onSearchQueryChange("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- Recently Played Song Card UI ---
@Composable
private fun RecentlyPlayedSongCard(
    song: Song,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(112.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            if (!song.albumArtUri.isNullOrBlank()) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_music_note),
                    contentDescription = song.title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --- Smart Shuffle Banner UI ---
@Composable
private fun SmartShuffleCard(onClick: () -> Unit) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), cardShape)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_sparkle),
                contentDescription = "Smart Shuffle",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Smart Shuffle",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Mix your library based on your listening habits.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Balanced · 40% randomness",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// --- Playlists Container UI ---
@Composable
private fun PlaylistsContainer(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit
) {
    val containerShape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(containerShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, containerShape)
    ) {
        playlists.forEachIndexed { index, playlist ->
            val iconRes = when (playlist.iconType) {
                PlaylistIconType.HEART -> R.drawable.ic_heart
                PlaylistIconType.MOON -> R.drawable.ic_moon
                PlaylistIconType.FIRE, PlaylistIconType.GYM -> R.drawable.ic_lightning
                else -> R.drawable.ic_music_note
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlaylistClick(playlist) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.verticalGradient(playlist.gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!playlist.customImageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = playlist.customImageUri,
                            contentDescription = playlist.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = playlist.name,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${playlist.songs.size} songs",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (index < playlists.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(start = 76.dp, end = 14.dp)
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    com.example.localliy.ui.theme.LocalliyTheme {
        HomeScreen(
            repository = remember { MusicRepository() }
        )
    }
}
