package com.example.localliy.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun LocalliyBottomNavBar(
    selectedDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val navShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val containerColor = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .clip(navShape)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = navShape
            )
            .background(containerColor),
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
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
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor,
                    indicatorColor = selectedColor.copy(alpha = 0.15f)
                )
            )
        }
    }
}
