package com.example.localliy

import android.Manifest
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localliy.data.model.RepeatMode
import com.example.localliy.data.model.Song
import com.example.localliy.data.repository.MusicRepository
import com.example.localliy.ui.components.LocalliyBottomNavBar
import com.example.localliy.ui.components.MiniPlayer
import com.example.localliy.ui.components.NavDestination
import com.example.localliy.ui.screens.HomeScreen
import com.example.localliy.ui.screens.LibraryScreen
import com.example.localliy.ui.screens.SettingsScreen
import com.example.localliy.ui.theme.AccentColor
import com.example.localliy.ui.theme.AppThemeMode
import com.example.localliy.ui.theme.LocalliyTheme
import com.example.localliy.ui.theme.ThemeState
import com.example.localliy.ui.theme.ThemeViewModel
import com.example.localliy.util.MediaNotificationManager
import com.example.localliy.util.PlaybackBroadcastReceiver
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState by themeViewModel.themeState.collectAsState()

            LocalliyTheme(
                themeMode = themeState.themeMode,
                accentColor = themeState.accentColor,
                customAccentColor = themeState.customAccentColor
            ) {
                MainAppScreen(
                    themeState = themeState,
                    onThemeModeSelected = { themeViewModel.setThemeMode(it) },
                    onAccentColorSelected = { themeViewModel.setAccentColor(it) },
                    onCustomAccentColorSelected = { themeViewModel.setCustomAccentColor(it) }
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(
    themeState: ThemeState = ThemeState(),
    onThemeModeSelected: (AppThemeMode) -> Unit = {},
    onAccentColorSelected: (AccentColor) -> Unit = {},
    onCustomAccentColorSelected: (Color) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedDestination by remember { mutableStateOf(NavDestination.HOME) }
    val musicRepository = remember { MusicRepository() }

    // Request Notification Permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Scan local media if accessible
    remember {
        musicRepository.scanLocalAudio(context)
    }

    var songsList by remember { mutableStateOf(musicRepository.getSongs()) }
    val restoredQueuePair = remember {
        try {
            musicRepository.getRestoredQueue(context)
        } catch (_: Exception) {
            Pair(songsList, "Library")
        }
    }
    var activeQueue by remember { mutableStateOf(restoredQueuePair.first) }
    var activeQueueName by remember { mutableStateOf(restoredQueuePair.second) }

    var currentSong by remember {
        mutableStateOf<Song?>(
            try {
                musicRepository.getLastPlayedSong(context) ?: activeQueue.firstOrNull() ?: songsList.firstOrNull()
            } catch (_: Exception) {
                songsList.firstOrNull()
            }
        )
    }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0.0f) }
    var isBottomBarVisible by remember { mutableStateOf(true) }

    var isShuffleEnabled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf(RepeatMode.OFF) }

    // Save last played song and exact queue state when changed
    LaunchedEffect(currentSong, activeQueueName, activeQueue) {
        if (currentSong != null) {
            val qName = activeQueueName
            val (qType, qId) = when {
                qName == "Liked Songs" -> Pair("PLAYLIST", "liked_songs")
                musicRepository.getCustomPlaylists().any { it.name.equals(qName, ignoreCase = true) } -> {
                    val pl = musicRepository.getCustomPlaylists().first { it.name.equals(qName, ignoreCase = true) }
                    Pair("PLAYLIST", pl.id)
                }
                musicRepository.getAlbums().any { it.name.equals(qName, ignoreCase = true) } -> Pair("ALBUM", qName)
                musicRepository.getArtists().any { it.name.equals(qName, ignoreCase = true) } -> Pair("ARTIST", qName)
                else -> Pair("LIBRARY", "")
            }
            musicRepository.saveLastPlaybackState(context, currentSong, qName, qType, qId)
        }
    }

    val isInPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val mediaPlayer = remember { if (!isInPreview) MediaPlayer() else null }

    // Next Track Helper with Shuffle & Repeat support
    fun playNextTrack() {
        val queue = if (activeQueue.isNotEmpty()) activeQueue else musicRepository.getSongs()
        if (queue.isEmpty()) return

        val song = currentSong

        if (repeatMode == RepeatMode.REPEAT_ONE) {
            try {
                mediaPlayer?.seekTo(0)
                if (isPlaying) mediaPlayer?.start()
            } catch (_: Exception) {}
            return
        }

        if (isShuffleEnabled) {
            val randomSong = queue.random()
            currentSong = randomSong
        } else {
            val currentIndex = queue.indexOfFirst { it.id == song?.id }
            if (currentIndex != -1 && currentIndex + 1 < queue.size) {
                currentSong = queue[currentIndex + 1]
            } else if (repeatMode == RepeatMode.REPEAT_ALL) {
                currentSong = queue.first()
            } else {
                isPlaying = false
            }
        }
    }

    // Previous Track Helper
    fun playPreviousTrack() {
        val queue = if (activeQueue.isNotEmpty()) activeQueue else musicRepository.getSongs()
        if (queue.isEmpty()) return

        val song = currentSong
        if (repeatMode == RepeatMode.REPEAT_ONE) {
            try {
                mediaPlayer?.seekTo(0)
                if (isPlaying) mediaPlayer?.start()
            } catch (_: Exception) {}
            return
        }

        if (isShuffleEnabled) {
            currentSong = queue.random()
        } else {
            val currentIndex = queue.indexOfFirst { it.id == song?.id }
            if (currentIndex > 0) {
                currentSong = queue[currentIndex - 1]
            } else {
                currentSong = queue.last()
            }
        }
    }

    // Wire up Broadcast Receiver callbacks for Media Notification controls
    DisposableEffect(Unit) {
        PlaybackBroadcastReceiver.onPlayPause = {
            isPlaying = !isPlaying
        }
        PlaybackBroadcastReceiver.onPrevious = {
            playPreviousTrack()
        }
        PlaybackBroadcastReceiver.onNext = {
            playNextTrack()
        }
        PlaybackBroadcastReceiver.onStop = {
            isPlaying = false
            MediaNotificationManager.cancelNotification(context)
        }

        onDispose {
            try {
                mediaPlayer?.release()
            } catch (_: Exception) {}
            MediaNotificationManager.cancelNotification(context)
        }
    }

    // Synchronize MediaPlayer looping state when repeatMode changes
    LaunchedEffect(repeatMode) {
        try {
            mediaPlayer?.isLooping = (repeatMode == RepeatMode.REPEAT_ONE)
        } catch (_: Exception) {}
    }

    // Audio Playback Controller
    LaunchedEffect(currentSong) {
        val song = currentSong
        if (song != null && song.contentUri != null && mediaPlayer != null) {
            musicRepository.addToRecentlyPlayed(song, context)
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(context, Uri.parse(song.contentUri))
                mediaPlayer.isLooping = (repeatMode == RepeatMode.REPEAT_ONE)
                mediaPlayer.prepare()
                if (isPlaying) {
                    mediaPlayer.start()
                }

                mediaPlayer.setOnCompletionListener {
                    if (repeatMode != RepeatMode.REPEAT_ONE) {
                        playNextTrack()
                    }
                }
            } catch (_: Exception) {
                // Audio file playback error
            }
        }
    }

    LaunchedEffect(isPlaying) {
        if (mediaPlayer != null) {
            try {
                if (isPlaying) {
                    if (!mediaPlayer.isPlaying && currentSong?.contentUri != null) {
                        mediaPlayer.start()
                    }
                } else {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // Sync Notification Status
    LaunchedEffect(currentSong, isPlaying, activeQueueName) {
        if (currentSong != null) {
            MediaNotificationManager.showNotification(
                context = context,
                song = currentSong,
                isPlaying = isPlaying,
                queueName = activeQueueName
            )
        } else {
            MediaNotificationManager.cancelNotification(context)
        }
    }

    // Progress Updater
    LaunchedEffect(isPlaying, currentSong) {
        while (isPlaying) {
            try {
                if (mediaPlayer != null && mediaPlayer.isPlaying && mediaPlayer.duration > 0) {
                    progress = mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration.toFloat()
                }
            } catch (_: Exception) {}
            delay(500)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isBottomBarVisible) {
                Column {
                    currentSong?.let { song ->
                        MiniPlayer(
                            currentSong = song,
                            isPlaying = isPlaying,
                            queueName = activeQueueName,
                            progress = progress,
                            onPlayPauseToggle = { isPlaying = !isPlaying },
                            onSkipNext = { playNextTrack() },
                            onSkipPrevious = { playPreviousTrack() }
                        )
                    }

                    LocalliyBottomNavBar(
                        selectedDestination = selectedDestination,
                        onDestinationSelected = { destination ->
                            selectedDestination = destination
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isBottomBarVisible) innerPadding else PaddingValues(0.dp))
        ) {
            when (selectedDestination) {

                NavDestination.HOME -> {
                    HomeScreen(
                        repository = musicRepository,
                        isShuffleEnabled = isShuffleEnabled,
                        repeatMode = repeatMode,
                        onToggleShuffle = { isShuffleEnabled = !isShuffleEnabled },
                        onToggleRepeat = {
                            repeatMode = when (repeatMode) {
                                RepeatMode.OFF -> RepeatMode.REPEAT_ALL
                                RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
                                RepeatMode.REPEAT_ONE -> RepeatMode.OFF
                            }
                        },
                        onSongSelected = { song ->
                            musicRepository.addToRecentlyPlayed(song, context)
                            currentSong = song
                            isPlaying = true
                        },
                        onSongLikedToggle = { song ->
                            if (currentSong?.id == song.id) {
                                currentSong = currentSong?.copy(isLiked = !song.isLiked)
                            }
                        },
                        onPlayAllQueue = { queueSongs, queueTitle ->
                            if (queueSongs.isNotEmpty()) {
                                activeQueue = queueSongs
                                activeQueueName = queueTitle
                                currentSong = if (isShuffleEnabled) queueSongs.random() else queueSongs.first()
                                isPlaying = true
                            }
                        }
                    )
                }

                NavDestination.LIBRARY -> {
                    LibraryScreen(
                        repository = musicRepository,
                        isShuffleEnabled = isShuffleEnabled,
                        repeatMode = repeatMode,
                        onToggleShuffle = { isShuffleEnabled = !isShuffleEnabled },
                        onToggleRepeat = {
                            repeatMode = when (repeatMode) {
                                RepeatMode.OFF -> RepeatMode.REPEAT_ALL
                                RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
                                RepeatMode.REPEAT_ONE -> RepeatMode.OFF
                            }
                        },
                        onPlayAllQueue = { queueSongs, queueTitle ->
                            if (queueSongs.isNotEmpty()) {
                                activeQueue = queueSongs
                                activeQueueName = queueTitle
                                currentSong = if (isShuffleEnabled) queueSongs.random() else queueSongs.first()
                                isPlaying = true
                            }
                        },
                        onSongSelected = { song ->
                            musicRepository.addToRecentlyPlayed(song, context)
                            currentSong = song
                            isPlaying = true
                        },
                        onSongLikedToggle = { song ->
                            if (currentSong?.id == song.id) {
                                currentSong = currentSong?.copy(isLiked = !song.isLiked)
                            }
                        },
                        onBottomBarVisibilityChanged = { visible ->
                            isBottomBarVisible = visible
                        }
                    )
                }

                NavDestination.SETTINGS -> {
                    SettingsScreen(
                        themeState = themeState,
                        onThemeModeSelected = onThemeModeSelected,
                        onAccentColorSelected = onAccentColorSelected,
                        onCustomAccentColorSelected = onCustomAccentColorSelected,
                        repository = musicRepository,
                        onStorageCleared = {
                            songsList = musicRepository.getSongs()
                            currentSong = null
                            isPlaying = false
                            try {
                                mediaPlayer?.reset()
                            } catch (_: Exception) {}
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppScreenPreview() {
    LocalliyTheme {
        MainAppScreen()
    }
}
