package com.example.localliy.ui.screens

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localliy.R

// ============================================================================
// 1. DATA MODELS & HARDCODED DATA
//    CHANGE PLAYLIST / RECENTLY PLAYED NAMES, SONGS & ICONS HERE
// ============================================================================

data class RecentlyPlayedData(
    val title: String,
    val subtitle: String,
    @param:DrawableRes val iconRes: Int,
    val gradientColors: List<Color>
)

data class PlaylistData(
    val title: String,
    val subtitle: String,
    @param:DrawableRes val iconRes: Int,
    val iconGradient: List<Color>
)

// List of Recently Played items shown in the horizontal row
val sampleRecentlyPlayed = listOf(
    RecentlyPlayedData(
        title = "Favourite Mix",
        subtitle = "42 songs",
        iconRes = R.drawable.ic_music_note, // Replace with custom image/drawable if desired
        gradientColors = listOf(Color(0xFF334636), Color(0xFF19231B))
    ),
    RecentlyPlayedData(
        title = "Indie Rock",
        subtitle = "Playlist",
        iconRes = R.drawable.ic_vinyl, // Replace with custom image/drawable if desired
        gradientColors = listOf(Color(0xFF3F4638), Color(0xFF1E241B))
    ),
    RecentlyPlayedData(
        title = "Night Drive",
        subtitle = "28 songs",
        iconRes = R.drawable.ic_music_note, // Replace with custom image/drawable if desired
        gradientColors = listOf(Color(0xFF2B3A44), Color(0xFF161E23))
    )
)

// List of Playlists shown in the "Your playlists" card
val samplePlaylists = listOf(
    PlaylistData(
        title = "Liked Songs",
        subtitle = "186 songs",
        iconRes = R.drawable.ic_heart, // Replace with custom icon or image resource
        iconGradient = listOf(Color(0xFF8B5342), Color(0xFF523126))
    ),
    PlaylistData(
        title = "Workout",
        subtitle = "64 songs",
        iconRes = R.drawable.ic_lightning, // Replace with custom icon or image resource
        iconGradient = listOf(Color(0xFF7A5638), Color(0xFF47311F))
    ),
    PlaylistData(
        title = "Late Night",
        subtitle = "31 songs",
        iconRes = R.drawable.ic_moon, // Replace with custom icon or image resource
        iconGradient = listOf(Color(0xFF6B5548), Color(0xFF3B2E26))
    )
)

// ============================================================================
// 2. MAIN HOME SCREEN COMPOSABLE
// ============================================================================

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1013))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        // --- Header ---
        Text(
            text = "Good evening",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ready for some music?",
            color = Color(0xFF9E9E9E),
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- Search Bar ---
        SearchBarUI(onClick = { onItemClick("Search Bar") })

        Spacer(modifier = Modifier.height(24.dp))

        // --- Recently Played Section ---
        Text(
            text = "Recently played",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(sampleRecentlyPlayed) { item ->
                RecentlyPlayedCard(
                    item = item,
                    onClick = { onItemClick("Recently Played: ${item.title}") }
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // --- Smart Shuffle Banner ---
        SmartShuffleCard(onClick = { onItemClick("Smart Shuffle") })

        Spacer(modifier = Modifier.height(26.dp))

        // --- Your Playlists Section ---
        Text(
            text = "Your playlists",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        PlaylistsContainer(
            playlists = samplePlaylists,
            onPlaylistClick = { playlist -> onItemClick("Playlist: ${playlist.title}") }
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ============================================================================
// 3. UI COMPONENTS
// ============================================================================

// --- Search Bar UI ---
@Composable
private fun SearchBarUI(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF161A1E))
            .border(1.dp, Color(0xFF252C33), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Search",
            tint = Color(0xFF8E95A2),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Search your music, playlists...",
            color = Color(0xFF8E95A2),
            fontSize = 15.sp
        )
    }
}

// --- Recently Played Card UI ---
@Composable
private fun RecentlyPlayedCard(
    item: RecentlyPlayedData,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(112.dp)
            .clickable { onClick() }
    ) {
        // Gradient Icon Box
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(item.gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.title,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Text(
            text = item.subtitle,
            color = Color(0xFF8E95A2),
            fontSize = 12.sp,
            maxLines = 1
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
                    colors = listOf(Color(0xFF102D1B), Color(0xFF091A10))
                )
            )
            .border(1.dp, Color(0xFF1F5231), cardShape)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_sparkle),
                contentDescription = "Smart Shuffle",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Smart Shuffle",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Mix your library based on your listening habits.",
            color = Color(0xFFA3B8AB),
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Green Pill Tag
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF163B23))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Balanced · 40% randomness",
                color = Color(0xFF82E29F),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// --- Your Playlists Container UI ---
@Composable
private fun PlaylistsContainer(
    playlists: List<PlaylistData>,
    onPlaylistClick: (PlaylistData) -> Unit
) {
    val containerShape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(containerShape)
            .background(Color(0xFF13171A))
            .border(1.dp, Color(0xFF22282F), containerShape)
    ) {
        playlists.forEachIndexed { index, playlist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlaylistClick(playlist) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Playlist Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.verticalGradient(playlist.iconGradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = playlist.iconRes),
                        contentDescription = playlist.title,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title & Song Count
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = playlist.subtitle,
                        color = Color(0xFF8E95A2),
                        fontSize = 13.sp
                    )
                }

                // Chevron Arrow
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Open",
                    tint = Color(0xFF8E95A2),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Divider between items
            if (index < playlists.lastIndex) {
                HorizontalDivider(
                    color = Color(0xFF22282F),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(start = 76.dp, end = 14.dp)
                )
            }
        }
    }
}
