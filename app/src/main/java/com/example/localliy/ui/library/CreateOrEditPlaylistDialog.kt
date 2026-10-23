package com.example.localliy.ui.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.localliy.data.model.Playlist
import com.example.localliy.data.model.PlaylistIconType
import com.example.localliy.data.model.PlaylistThemes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrEditPlaylistDialog(
    existingPlaylist: Playlist? = null,
    onSavePlaylist: (
        name: String,
        description: String,
        iconType: PlaylistIconType,
        gradientColors: List<Color>,
        customImageUri: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(existingPlaylist?.name ?: "") }
    var description by remember { mutableStateOf(existingPlaylist?.description ?: "") }
    var selectedIcon by remember { mutableStateOf(existingPlaylist?.iconType ?: PlaylistIconType.MUSIC) }
    var selectedTheme by remember {
        mutableStateOf(
            PlaylistThemes.allThemes.firstOrNull { it.colors == existingPlaylist?.gradientColors }
                ?: PlaylistThemes.EmeraldGreen
        )
    }
    var customImageUri by remember { mutableStateOf(existingPlaylist?.customImageUri) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            customImageUri = it.toString()
        }
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF181D24))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (existingPlaylist == null) "Create Playlist" else "Edit Playlist",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cover Preview & Image Picker Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.linearGradient(colors = selectedTheme.colors)
                            )
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (customImageUri != null) {
                            AsyncImage(
                                model = customImageUri,
                                contentDescription = "Cover Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(72.dp)
                            )
                        } else {
                            val iconVector = when (selectedIcon) {
                                PlaylistIconType.HEART -> Icons.Default.Favorite
                                PlaylistIconType.FIRE -> Icons.Default.LocalFireDepartment
                                PlaylistIconType.STAR -> Icons.Default.Star
                                PlaylistIconType.HEADPHONES -> Icons.Default.Headphones
                                PlaylistIconType.WAVE -> Icons.Default.GraphicEq
                                PlaylistIconType.SPARKLES -> Icons.Default.AutoAwesome
                                else -> Icons.Default.MusicNote
                            }
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2B333E),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (customImageUri == null) "Pick Picture" else "Change Picture", fontSize = 13.sp)
                        }

                        if (customImageUri != null) {
                            TextButton(
                                onClick = { customImageUri = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Remove picture", color = Color(0xFFEF4444), fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name", color = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF12161A),
                        unfocusedContainerColor = Color(0xFF12161A),
                        focusedBorderColor = ActiveGreen,
                        unfocusedBorderColor = Color(0xFF2E3642),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)", color = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF12161A),
                        unfocusedContainerColor = Color(0xFF12161A),
                        focusedBorderColor = ActiveGreen,
                        unfocusedBorderColor = Color(0xFF2E3642),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Color Theme Picker
                Text("Theme Color", color = Color(0xFFD1D5DB), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(PlaylistThemes.allThemes) { theme ->
                        val isSelected = theme.id == selectedTheme.id
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(brush = Brush.linearGradient(colors = theme.colors))
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedTheme = theme }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Picker
                Text("Playlist Icon", color = Color(0xFFD1D5DB), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                val availableIcons = listOf(
                    PlaylistIconType.MUSIC to Icons.Default.MusicNote,
                    PlaylistIconType.HEART to Icons.Default.Favorite,
                    PlaylistIconType.FIRE to Icons.Default.LocalFireDepartment,
                    PlaylistIconType.STAR to Icons.Default.Star,
                    PlaylistIconType.HEADPHONES to Icons.Default.Headphones,
                    PlaylistIconType.WAVE to Icons.Default.GraphicEq,
                    PlaylistIconType.SPARKLES to Icons.Default.AutoAwesome
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(availableIcons) { (iconType, iconVector) ->
                        val isSelected = iconType == selectedIcon
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ActiveGreen else Color(0xFF252D37))
                                .clickable { selectedIcon = iconType },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color(0xFF9CA3AF))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSavePlaylist(
                                    name.trim(),
                                    description.trim(),
                                    selectedIcon,
                                    selectedTheme.colors,
                                    customImageUri
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActiveGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank()
                    ) {
                        Text(if (existingPlaylist == null) "Create" else "Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
