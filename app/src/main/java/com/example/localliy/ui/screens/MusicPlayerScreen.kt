package com.example.localliy.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.localliy.R
import com.example.localliy.data.model.Playlist
import com.example.localliy.data.model.RepeatMode
import com.example.localliy.data.model.Song
import com.example.localliy.ui.library.SelectPlaylistForSongDialog
import com.example.localliy.ui.theme.LocalliyTheme
import java.util.Locale

@Composable
fun MusicPlayerScreen(
    currentSong: Song,
    isPlaying: Boolean,
    queueName: String,
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    progress: Float,
    playlists: List<Playlist> = emptyList(),
    onPlayPauseToggle: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onSelectPlaylist: (Playlist) -> Unit = {},
    onCreateNewPlaylist: () -> Unit = {},
    onSeek: (Float) -> Unit = {},
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onClose()
    }

    val accentColor = MaterialTheme.colorScheme.primary
    val playButtonBg = MaterialTheme.colorScheme.primary
    val playButtonIconTint = MaterialTheme.colorScheme.onPrimary
    val mainTextColor = MaterialTheme.colorScheme.onBackground
    val subtitleTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val inactiveControlTint = MaterialTheme.colorScheme.onSurfaceVariant

    var isMenuExpanded by remember { mutableStateOf(false) }
    var showSelectPlaylistDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Full Screen Click Interceptor Backdrop Button ---
        // Consumes all touch events below inputs to prevent click-throughs to screens behind
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Intentionally empty to block click-throughs
                )
        )

        // --- Main Player Layout ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Top Bar with 3 Dots Menu ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = mainTextColor
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PLAYING FROM",
                        color = subtitleTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = queueName.ifBlank { "Library" },
                        color = mainTextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = mainTextColor
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add to Playlist", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.PlaylistAdd,
                                    contentDescription = null,
                                    tint = accentColor
                                )
                            },
                            onClick = {
                                isMenuExpanded = false
                                showSelectPlaylistDialog = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Center Artwork ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val hasArt = !currentSong.albumArtUri.isNullOrBlank() && !currentSong.albumArtUri.endsWith("/0")
                if (hasArt) {
                    AsyncImage(
                        model = currentSong.albumArtUri,
                        contentDescription = currentSong.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = painterResource(id = R.drawable.ic_music_note),
                        fallback = painterResource(id = R.drawable.ic_music_note)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_music_note),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Song Title & Artist ---
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentSong.title,
                    color = mainTextColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentSong.artist,
                    color = subtitleTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Progress Slider & Timestamps ---
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = progress.coerceIn(0f, 1f),
                    onValueChange = { onSeek(it) },
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentSecs = ((currentSong.durationMs * progress.coerceIn(0f, 1f)) / 1000).toLong()
                    Text(
                        text = formatDuration(currentSecs * 1000),
                        color = subtitleTextColor,
                        fontSize = 12.sp
                    )
                    Text(
                        text = currentSong.formattedDuration,
                        color = subtitleTextColor,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Playback Controls Row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Shuffle Button
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) accentColor else inactiveControlTint,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Previous Button
                IconButton(onClick = onSkipPrevious) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = mainTextColor,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play/Pause Circular Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(playButtonBg)
                        .clickable { onPlayPauseToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = playButtonIconTint,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next Button
                IconButton(onClick = onSkipNext) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = mainTextColor,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat Button
                IconButton(onClick = onToggleRepeat) {
                    val icon = if (repeatMode == RepeatMode.REPEAT_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                    val tint = if (repeatMode != RepeatMode.OFF) accentColor else inactiveControlTint
                    Icon(
                        imageVector = icon,
                        contentDescription = "Repeat",
                        tint = tint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- Add to Playlist Dialog ---
        if (showSelectPlaylistDialog) {
            SelectPlaylistForSongDialog(
                song = currentSong,
                playlists = playlists,
                onSelectPlaylist = { selectedPlaylist ->
                    onSelectPlaylist(selectedPlaylist)
                    showSelectPlaylistDialog = false
                },
                onCreateNewPlaylist = {
                    showSelectPlaylistDialog = false
                    onCreateNewPlaylist()
                },
                onDismiss = { showSelectPlaylistDialog = false }
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun MusicPlayerScreenPreview() {
    LocalliyTheme {
        MusicPlayerScreen(
            currentSong = Song(
                id = "demo",
                title = "Do I Wanna Know?",
                artist = "Arctic Monkeys",
                album = "AM",
                durationMs = 272000L
            ),
            isPlaying = true,
            queueName = "Liked Songs",
            isShuffleEnabled = true,
            repeatMode = RepeatMode.REPEAT_ALL,
            progress = 0.38f,
            onPlayPauseToggle = {},
            onSkipNext = {},
            onSkipPrevious = {},
            onToggleShuffle = {},
            onToggleRepeat = {},
            onClose = {}
        )
    }
}
