package com.example.localliy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
                accentColor = themeState.accentColor
            ) {
                MainAppScreen(
                    themeState = themeState,
                    onThemeModeSelected = { themeViewModel.setThemeMode(it) },
                    onAccentColorSelected = { themeViewModel.setAccentColor(it) }
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(
    themeState: ThemeState = ThemeState(),
    onThemeModeSelected: (AppThemeMode) -> Unit = {},
    onAccentColorSelected: (AccentColor) -> Unit = {}
) {
    var selectedDestination by remember { mutableStateOf(NavDestination.HOME) }

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
                NavDestination.HOME -> HomeScreen()
                NavDestination.LIBRARY -> LibraryScreen()
                NavDestination.SETTINGS -> SettingsScreen(
                    themeState = themeState,
                    onThemeModeSelected = onThemeModeSelected,
                    onAccentColorSelected = onAccentColorSelected
                )
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
