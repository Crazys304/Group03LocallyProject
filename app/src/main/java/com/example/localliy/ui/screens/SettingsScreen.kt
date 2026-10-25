package com.example.localliy.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localliy.data.repository.MusicRepository
import com.example.localliy.ui.library.ActiveGreen
import com.example.localliy.ui.theme.AccentColor
import com.example.localliy.ui.theme.AppThemeMode
import com.example.localliy.ui.theme.ThemeState
import kotlin.math.ceil

@Composable
fun TranslucentCheckerboardBackground(
    modifier: Modifier = Modifier,
    squareSizeDp: Dp = 6.dp
) {
    val isDark = isSystemInDarkTheme()
    val color1 = if (isDark) Color(0xFF2A2E33) else Color(0xFFE2E8F0)
    val color2 = if (isDark) Color(0xFF1E2227) else Color(0xFFCBD5E1)

    Canvas(modifier = modifier) {
        val squareSizePx = squareSizeDp.toPx()
        val numCols = ceil(size.width / squareSizePx).toInt()
        val numRows = ceil(size.height / squareSizePx).toInt()

        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                val color = if ((row + col) % 2 == 0) color1 else color2
                drawRect(
                    color = color,
                    topLeft = Offset(col * squareSizePx, row * squareSizePx),
                    size = Size(squareSizePx, squareSizePx)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: MusicRepository? = null,
    onStorageCleared: () -> Unit = {},
    themeState: ThemeState = ThemeState(),
    onThemeModeSelected: (AppThemeMode) -> Unit = {},
    onAccentColorSelected: (AccentColor) -> Unit = {},
    onCustomAccentColorSelected: (Color) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isThemeExpanded by rememberSaveable { mutableStateOf(false) }
    var isAccentExpanded by rememberSaveable { mutableStateOf(true) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && repository != null) {
            repository.isPermissionGranted = true
            val count = repository.scanLocalAudio(context)
            Toast.makeText(context, "Scanned $count songs from device storage", Toast.LENGTH_SHORT).show()
            onStorageCleared()
        } else {
            Toast.makeText(context, "Storage permission required to scan songs", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Customize app appearance, theme & storage",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section 1: Theme Mode
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isThemeExpanded = !isThemeExpanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Theme Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = themeState.themeMode.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    ChevronIcon(isExpanded = isThemeExpanded)
                }

                AnimatedVisibility(visible = isThemeExpanded) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            val isSelected = themeState.themeMode == mode

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        else Color.Transparent
                                    )
                                    .clickable { onThemeModeSelected(mode) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onThemeModeSelected(mode) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Accent Color Box (RGB + Custom)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                val currentAccentColor = if (themeState.accentColor == AccentColor.CUSTOM) themeState.customAccentColor else themeState.accentColor.defaultColor

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isAccentExpanded = !isAccentExpanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Accent Color",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(currentAccentColor)
                            )
                            Text(
                                text = themeState.accentColor.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    ChevronIcon(isExpanded = isAccentExpanded)
                }

                AnimatedVisibility(visible = isAccentExpanded) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Select Red, Green, Blue, or pick a Custom accent color",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Swatches
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AccentColor.entries.forEach { accent ->
                                val isSelected = themeState.accentColor == accent
                                val swatchColor = if (accent == AccentColor.CUSTOM) themeState.customAccentColor else accent.defaultColor
                                val animatedBorderColor by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    label = "border_color"
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onAccentColorSelected(accent) }
                                        .padding(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .border(
                                                width = 3.dp,
                                                color = animatedBorderColor,
                                                shape = CircleShape
                                            )
                                            .padding(4.dp)
                                            .clip(CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        TranslucentCheckerboardBackground(
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(swatchColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = accent.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Custom Color Controls
                        AnimatedVisibility(visible = themeState.accentColor == AccentColor.CUSTOM) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )

                                Text(
                                    text = "Custom RGB Sliders & Hex Input",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Presets
                                val customPresets = listOf(
                                    Color(0xFF8B5CF6),
                                    Color(0xFFEC4899),
                                    Color(0xFF06B6D4),
                                    Color(0xFFF97316),
                                    Color(0xFFEAB308),
                                    Color(0xFF14B8A6)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Presets:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    customPresets.forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .clickable { onCustomAccentColorSelected(color) }
                                        )
                                    }
                                }

                                val currentRed = (themeState.customAccentColor.red * 255).toInt()
                                val currentGreen = (themeState.customAccentColor.green * 255).toInt()
                                val currentBlue = (themeState.customAccentColor.blue * 255).toInt()

                                // Red Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Red Channel",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = currentRed.toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red
                                        )
                                    }
                                    Slider(
                                        value = currentRed.toFloat(),
                                        onValueChange = { newRed ->
                                            onCustomAccentColorSelected(
                                                Color(newRed.toInt(), currentGreen, currentBlue)
                                            )
                                        },
                                        valueRange = 0f..255f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color.Red,
                                            activeTrackColor = Color.Red
                                        )
                                    )
                                }

                                // Green Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Green Channel",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = currentGreen.toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF22C55E)
                                        )
                                    }
                                    Slider(
                                        value = currentGreen.toFloat(),
                                        onValueChange = { newGreen ->
                                            onCustomAccentColorSelected(
                                                Color(currentRed, newGreen.toInt(), currentBlue)
                                            )
                                        },
                                        valueRange = 0f..255f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF22C55E),
                                            activeTrackColor = Color(0xFF22C55E)
                                        )
                                    )
                                }

                                // Blue Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Blue Channel",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = currentBlue.toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF3B82F6)
                                        )
                                    }
                                    Slider(
                                        value = currentBlue.toFloat(),
                                        onValueChange = { newBlue ->
                                            onCustomAccentColorSelected(
                                                Color(currentRed, currentGreen, newBlue.toInt())
                                            )
                                        },
                                        valueRange = 0f..255f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF3B82F6),
                                            activeTrackColor = Color(0xFF3B82F6)
                                        )
                                    )
                                }

                                // Hex Input Field
                                var hexText by remember(themeState.customAccentColor) {
                                    mutableStateOf(
                                        String.format("#%06X", 0xFFFFFF and themeState.customAccentColor.toArgb())
                                    )
                                }

                                OutlinedTextField(
                                    value = hexText,
                                    onValueChange = { input ->
                                        hexText = input
                                        val cleanHex = input.removePrefix("#").trim()
                                        if (cleanHex.length == 6) {
                                            runCatching {
                                                val parsedInt = cleanHex.toLong(16).toInt()
                                                val color = Color(0xFF000000 or parsedInt.toLong())
                                                onCustomAccentColorSelected(color)
                                            }
                                        }
                                    },
                                    label = { Text("Hex Color Code (#RRGGBB)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Storage & Library Management
        repository?.let { repo ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = ActiveGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Music Library & Storage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Scanned Tracks: ${repo.allSongs.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(permissionToRequest)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ActiveGreen,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rescan", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showClearConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Data", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 4: WhatsApp Exclusion Filter Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = ActiveGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WhatsApp Exclusion Active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "WhatsApp voice notes and audio clips are automatically excluded from your music library.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section 5: App Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = ActiveGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Localliy Music Player",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Version 1.0.0 • Offline Storage Player",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Clear Confirmation Dialog
    if (showClearConfirmDialog && repository != null) {
        BasicAlertDialog(onDismissRequest = { showClearConfirmDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1B2027))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Clear Storage & Playlists?",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This will wipe all saved custom playlists and clear the scanned library cache. You can rescan your device music anytime.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showClearConfirmDialog = false }) {
                            Text("Cancel", color = Color(0xFF9CA3AF))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                repository.clearStorageAndReset(context)
                                showClearConfirmDialog = false
                                Toast.makeText(context, "Storage and playlists cleared", Toast.LENGTH_SHORT).show()
                                onStorageCleared()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEF4444),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Clear All", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChevronIcon(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevron_rotation"
    )
    Canvas(
        modifier = modifier
            .size(24.dp)
            .graphicsLayer { rotationZ = rotation }
    ) {
        val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.35f)
            lineTo(size.width * 0.5f, size.height * 0.65f)
            lineTo(size.width * 0.75f, size.height * 0.35f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
