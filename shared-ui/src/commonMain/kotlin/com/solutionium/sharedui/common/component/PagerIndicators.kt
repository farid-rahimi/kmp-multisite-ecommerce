package com.solutionium.sharedui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun LinePagerIndicators(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = LocalContentColor.current.copy(alpha = 0.3f),
    indicatorHeight: Dp = 4.dp,       // Height of the lines
    indicatorBaseWidth: Dp = 12.dp,   // Width of inactive lines
    activeIndicatorWidth: Dp = 24.dp, // Width of the active line
    spacing: Dp = 6.dp,
    indicatorShape: RoundedCornerShape = RoundedCornerShape(2.dp) // Optional: for rounded line ends
) {
    if (pageCount <= 0) return // Don't show if no pages

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { iteration ->
            val isSelected = currentPage == iteration
            val width = if (isSelected) activeIndicatorWidth else indicatorBaseWidth
            val color = if (isSelected) activeColor else inactiveColor

            Box(
                modifier = Modifier
                    .height(indicatorHeight)
                    .width(width)
                    .clip(indicatorShape) // Apply shape for rounded ends
                    .background(color)
            )
        }
    }
}