package com.example.localliy.ui.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class LibraryCategory(val displayName: String) {
    PLAYLISTS("Playlists"),
    ARTISTS("Artists"),
    ALBUMS("Albums"),
    SONGS("Songs")
}

val ActiveGreen = Color(0xFF22C55E)
val ActiveGreenBg = Color(0xFF143822)
val InactiveChipBg = Color(0xFF1E242C)
val InactiveText = Color(0xFF9CA3AF)

@Composable
fun CategoryFilterChips(
    selectedCategory: LibraryCategory,
    onCategorySelected: (LibraryCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(LibraryCategory.entries.toTypedArray()) { category ->
            val isSelected = category == selectedCategory

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) ActiveGreenBg else InactiveChipBg,
                label = "chipBg"
            )

            val borderColor by animateColorAsState(
                targetValue = if (isSelected) ActiveGreen else Color.Transparent,
                label = "chipBorder"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else InactiveText,
                label = "chipText"
            )

            val pillShape = RoundedCornerShape(50)

            Box(
                modifier = Modifier
                    .clip(pillShape)
                    .border(width = 1.dp, color = borderColor, shape = pillShape)
                    .background(backgroundColor)
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = category.displayName,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
