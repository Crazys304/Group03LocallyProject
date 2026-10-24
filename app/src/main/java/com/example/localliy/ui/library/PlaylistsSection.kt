package com.example.localliy.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.localliy.data.model.Playlist
import com.example.localliy.data.model.PlaylistIconType

@Composable
fun PlaylistsSection(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    onEditPlaylist: (Playlist) -> Unit,
    onAddSongsToPlaylist: (Playlist) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    if (playlists.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No playlists found",
                color = Color(0xFF9CA3AF),
                fontSize = 16.sp
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
    ) {
        items(playlists, key = { it.id }) { playlist ->
            PlaylistItemCard(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist) },
                onEdit = { onEditPlaylist(playlist) },
                onAddSongs = { onAddSongsToPlaylist(playlist) },
                onDelete = { onDeletePlaylist(playlist) }
            )
        }
    }
}

@Composable
fun PlaylistItemCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onAddSongs: () -> Unit,
    onDelete: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    var isMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color(0xFF14191F))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gradient artwork box OR custom picture
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    brush = Brush.linearGradient(colors = playlist.gradientColors)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (playlist.customImageUri != null) {
                AsyncImage(
                    model = playlist.customImageUri,
                    contentDescription = playlist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(56.dp)
                )
            } else {
                val iconVector = when (playlist.iconType) {
                    PlaylistIconType.HEART -> Icons.Default.Favorite
                    PlaylistIconType.GYM -> Icons.Default.FitnessCenter
                    PlaylistIconType.CAR -> Icons.Default.DirectionsCar
                    PlaylistIconType.MOON -> Icons.Default.NightlightRound
                    PlaylistIconType.LEAF -> Icons.Default.Spa
                    PlaylistIconType.FIRE -> Icons.Default.LocalFireDepartment
                    PlaylistIconType.STAR -> Icons.Default.Star
                    PlaylistIconType.HEADPHONES -> Icons.Default.Headphones
                    PlaylistIconType.WAVE -> Icons.Default.GraphicEq
                    PlaylistIconType.SPARKLES -> Icons.Default.AutoAwesome
                    PlaylistIconType.MUSIC -> Icons.Default.MusicNote
                }

                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Playlist details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = playlist.name,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${playlist.songCount} songs" + if (playlist.description.isNotBlank()) " • ${playlist.description}" else "",
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // More options dropdown
        Box {
            IconButton(onClick = { isMenuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E242C))
            ) {
                if (playlist.id != "liked_songs") {
                    DropdownMenuItem(
                        text = { Text("Add Songs", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = ActiveGreen) },
                        onClick = {
                            isMenuExpanded = false
                            onAddSongs()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit Playlist Look", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = ActiveGreen) },
                        onClick = {
                            isMenuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Playlist", color = Color(0xFFEF4444)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                        onClick = {
                            isMenuExpanded = false
                            onDelete()
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("View Liked Songs", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = ActiveGreen) },
                        onClick = {
                            isMenuExpanded = false
                            onClick()
                        }
                    )
                }
            }
        }
    }
}
