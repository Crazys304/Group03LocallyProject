package com.example.localliy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.background

import androidx.activity.viewModels

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.localliy.ui.components.LocalliyBottomNavBar
import com.example.localliy.ui.components.NavDestination
import com.example.localliy.ui.screens.HomeScreen

import com.example.localliy.ui.screens.LibraryScreen
import com.example.localliy.ui.screens.SettingsScreen
import com.example.localliy.ui.theme.AccentColor
import com.example.localliy.ui.theme.AppThemeMode

import com.example.localliy.ui.theme.LocalliyTheme
import com.example.localliy.ui.theme.ThemeState
import com.example.localliy.ui.theme.ThemeViewModel

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
    var selectedDestination by remember { mutableStateOf(NavDestination.HOME) }
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            LocalliyBottomNavBar(
                selectedDestination = selectedDestination,
                onDestinationSelected = { destination ->
                    selectedDestination = destination
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedDestination) {

                NavDestination.HOME -> {
                    HomeScreen(
                        onItemClick = { clickedItemName ->
                            Toast.makeText(context, "Clicked: $clickedItemName", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                NavDestination.LIBRARY -> LibraryScreen()
                NavDestination.SETTINGS -> SettingsScreen(
                    themeState = themeState,
                    onThemeModeSelected = onThemeModeSelected,
                    onAccentColorSelected = onAccentColorSelected,
                    onCustomAccentColorSelected = onCustomAccentColorSelected
                )

            }
        }
    }
}

@Composable
fun SimplePlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1013)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppScreenPreview() {
    LocalliyTheme {
        MainAppScreen()
    }
}
