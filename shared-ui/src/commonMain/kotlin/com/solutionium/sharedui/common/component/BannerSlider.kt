package com.solutionium.sharedui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.solutionium.shared.data.model.BannerItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.absoluteValue

@Composable
fun BannerSlider(
    modifier: Modifier = Modifier,
    items: List<BannerItem>,
    initialPage: Int = 0,
    autoScrollDurationMillis: Long = 8000,
    onBannerClick: (item: BannerItem) -> Unit
) {
    if (items.isEmpty()) {
        // Optionally, show a placeholder or nothing
        Spacer(modifier = modifier
            .height(200.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant))
        return
    }

    val actualItemCount = items.size
    val virtualItemCount = if (actualItemCount > 1) Int.MAX_VALUE else actualItemCount
    val effectiveInitialPage = if (actualItemCount > 1) (virtualItemCount / 2) - ((virtualItemCount / 2) % actualItemCount) + initialPage else 0


    val pagerState = rememberPagerState(
        initialPage = effectiveInitialPage,
        pageCount = { virtualItemCount }
    )
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    // More robust Auto-scroll effect
    if (autoScrollDurationMillis > 0 && actualItemCount > 1) {
        LaunchedEffect(key1 = Unit) { // Runs once and stays active
            while (isActive) { // Loop while the coroutine is active
                delay(autoScrollDurationMillis)
                if (!isDragged && isActive) {
                    try {
                        val currentPage = pagerState.currentPage
                        val nextPage = if (currentPage < virtualItemCount -1) currentPage + 1 else effectiveInitialPage // Loop back to a similar starting middle point
                        if (isActive) {
                            pagerState.animateScrollToPage(nextPage)
                        }
                    } catch (e: Exception) {
                        if (isActive) { // Log or handle only if still active
                            println("Error during auto-scroll: ${e.message}")
                        }
                    }
                }
            }
        }
    }


    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally // Center indicators horizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            // contentPadding = PaddingValues(horizontal = 32.dp), // Optional: for peek-through effect
            // pageSpacing = 16.dp, // Optional: space between pages
        ) { virtualPage ->
            val actualPage = virtualPage % actualItemCount // Map virtual page to actual item index
            val bannerItem = items[actualPage]

            BannerCard(
                bannerItem = bannerItem,
                onClick = { onBannerClick(bannerItem) },
                modifier = Modifier
                    // Example of a subtle parallax or scaling effect:
                    .graphicsLayer {
                        val pageOffset = (
                                (pagerState.currentPage - virtualPage) + pagerState
                                    .currentPageOffsetFraction
                                ).absoluteValue
                        // We apply the animation only when type is DRAG OR ANIMATE
                        alpha = lerp(
                            start = 0.6f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleY = lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
            )
        }

        // Page Indicators
        if (actualItemCount > 1) {
            LinePagerIndicators( // Use the new LinePagerIndicators
                pageCount = actualItemCount,
                currentPage = pagerState.currentPage % actualItemCount,
                modifier = Modifier
                    //.align(Alignment.BottomCenter) // No longer needed for alignment within the Box
                    .padding(vertical = 0.dp) // Adjust padding as needed
            )
        }
    }
}

