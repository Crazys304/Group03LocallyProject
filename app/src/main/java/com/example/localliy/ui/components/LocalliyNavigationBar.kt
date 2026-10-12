package com.example.localliy.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localliy.R

enum class NavDestination(
    val title: String,
    @param:DrawableRes val iconRes: Int,
) {
    HOME("Home", R.drawable.ic_nav_home),
    LIBRARY("Library", R.drawable.ic_nav_library),
    SETTINGS("Settings", R.drawable.ic_nav_settings)
}

val NavGreen = Color(0xFF22C55E)
val NavInactive = Color(0xFF8E95A2)
val NavBackground = Color(0xFF13171A)
val NavBorder = Color(0xFF242B33)

@Composable
fun LocalliyBottomNavBar(
    selectedDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val navShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .clip(navShape)
            .border(
                width = 1.dp,
                color = NavBorder,
                shape = navShape
            )
            .background(NavBackground),
        containerColor = NavBackground,
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        NavDestination.entries.forEach { destination ->
            val isSelected = destination == selectedDestination

            NavigationBarItem(
                selected = isSelected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        painter = painterResource(id = destination.iconRes),
                        contentDescription = destination.title
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavGreen,
                    selectedTextColor = NavGreen,
                    unselectedIconColor = NavInactive,
                    unselectedTextColor = NavInactive,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
