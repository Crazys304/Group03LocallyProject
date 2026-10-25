package com.example.localliy.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.localliy.data.model.RepeatMode
import com.example.localliy.data.model.Song
import com.example.localliy.data.repository.MusicRepository

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
    refreshTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    com.example.localliy.ui.library.LibraryScreen(
        repository = repository,
        onSongSelected = onSongSelected,
        onPlayAllQueue = onPlayAllQueue,
        isShuffleEnabled = isShuffleEnabled,
        repeatMode = repeatMode,
        onToggleShuffle = onToggleShuffle,
        onToggleRepeat = onToggleRepeat,
        onSongLikedToggle = onSongLikedToggle,
        onBottomBarVisibilityChanged = onBottomBarVisibilityChanged,
        modifier = modifier
    )
}
