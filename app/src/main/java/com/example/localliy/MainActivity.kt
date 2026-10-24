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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.example.localliy.ui.library.LibraryScreen
import com.example.localliy.ui.theme.LocalliyTheme
import com.example.localliy.util.MediaNotificationManager
import com.example.localliy.util.PlaybackBroadcastReceiver
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalliyTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    var selectedDestination by remember { mutableStateOf(NavDestination.LIBRARY) }
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
    var activeQueue by remember { mutableStateOf(songsList) }
    var activeQueueName by remember { mutableStateOf("Library") }

    var currentSong by remember {
        mutableStateOf<Song?>(songsList.firstOrNull())
    }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0.0f) }
    var isBottomBarVisible by remember { mutableStateOf(true) }

    var isShuffleEnabled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf(RepeatMode.OFF) }

    val mediaPlayer = remember { MediaPlayer() }

    // Next Track Helper with Shuffle & Repeat support
    fun playNextTrack() {
        val queue = if (activeQueue.isNotEmpty()) activeQueue else musicRepository.getSongs()
        if (queue.isEmpty()) return

        val song = currentSong
        if (repeatMode == RepeatMode.REPEAT_ONE && song != null) {
            // Replay same song
            currentSong = song.copy()
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
                mediaPlayer.release()
            } catch (_: Exception) {}
            MediaNotificationManager.cancelNotification(context)
        }
    }

    // Audio Playback Controller
    LaunchedEffect(currentSong) {
        val song = currentSong
        if (song != null && song.contentUri != null) {
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(context, Uri.parse(song.contentUri))
                mediaPlayer.prepare()
                if (isPlaying) {
                    mediaPlayer.start()
                }

                mediaPlayer.setOnCompletionListener {
                    playNextTrack()
                }
            } catch (_: Exception) {
                // Audio file playback error
            }
        }
    }

    LaunchedEffect(isPlaying) {
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
                if (mediaPlayer.isPlaying && mediaPlayer.duration > 0) {
                    progress = mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration.toFloat()
                }
            } catch (_: Exception) {}
            delay(500)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0F1215),
        bottomBar = {
            if (isBottomBarVisible) {
                Column {
                    currentSong?.let { song ->
                        MiniPlayer(
                            currentSong = song,
                            isPlaying = isPlaying,
                            queueName = activeQueueName,
                            isShuffleEnabled = isShuffleEnabled,
                            repeatMode = repeatMode,
                            progress = progress,
                            onPlayPauseToggle = { isPlaying = !isPlaying },
                            onSkipNext = { playNextTrack() },
                            onSkipPrevious = { playPreviousTrack() },
                            onToggleShuffle = { isShuffleEnabled = !isShuffleEnabled },
                            onToggleRepeat = {
                                repeatMode = when (repeatMode) {
                                    RepeatMode.OFF -> RepeatMode.REPEAT_ALL
                                    RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
                                    RepeatMode.REPEAT_ONE -> RepeatMode.OFF
                                }
                            },
                            onToggleLike = { target ->
                                musicRepository.toggleLike(target.id)
                                songsList = musicRepository.getSongs()
                                if (currentSong?.id == target.id) {
                                    currentSong = currentSong?.copy(isLiked = !target.isLiked)
                                }
                            }
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
                NavDestination.HOME -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Home Screen",
                            color = Color.White,
                            fontSize = 24.sp
                        )
                    }
                }
                NavDestination.SETTINGS -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Settings Screen",
                            color = Color.White,
                            fontSize = 24.sp
                        )
                    }
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
