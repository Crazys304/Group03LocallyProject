package com.example.localliy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.localliy.ui.components.LocalliyBottomNavBar
import com.example.localliy.ui.components.NavDestination
import com.example.localliy.ui.theme.LocalliyTheme

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
    var selectedDestination by remember { mutableStateOf(NavDestination.HOME) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0F1215),
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
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${selectedDestination.title} Screen",
                color = Color.White,
                fontSize = 24.sp
            )
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
