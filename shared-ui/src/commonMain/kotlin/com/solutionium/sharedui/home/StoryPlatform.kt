package com.solutionium.sharedui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import coil3.compose.AsyncImage
import com.solutionium.shared.data.model.Link
import com.solutionium.shared.data.model.StoryItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

@Composable
fun PlatformStoryReelSection(
    stories: List<StoryItem>,
    onStoryClick: (StoryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(stories) { story ->
            PlatformStoryPreviewItem(
                story = story,
                onClick = { onStoryClick(story) },
            )
        }
    }
}

@Composable
private fun PlatformStoryPreviewItem(
    story: StoryItem,
    onClick: () -> Unit,
) {
    val ringColors = if (story.isViewed) {
        listOf(Color.LightGray, Color.LightGray)
    } else {
        listOf(Color(0xFF833AB4), Color(0xFFF77737), Color(0xFFE1306C))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier.size(64.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.dp, Brush.verticalGradient(ringColors)),
            ) {
                AsyncImage(
                    model = story.mediaUrl,
                    contentDescription = story.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(3.dp)
                        .clip(CircleShape),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = story.title,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PlatformStoryViewer(
    stories: List<StoryItem>,
    startIndex: Int,
    onClose: () -> Unit,
    onLinkClick: (Link) -> Unit,
    onStoryViewed: (storyId: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val storyDurationMs = 5000
    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, (stories.lastIndex).coerceAtLeast(0)),
        pageCount = { stories.size },
    )
    val progress = remember { Animatable(0f) }
    val verticalOffset = remember { Animatable(0f) }
    var isVerticalDragging by remember { mutableStateOf(false) }
    var isProgressPaused by remember { mutableStateOf(false) }
    var lastTransitionTarget by remember { mutableIntStateOf(pagerState.settledPage) }
    var isClosing by remember { mutableStateOf(false) }
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 120.dp.toPx() }
    fun closeViewer() {
        if (isClosing) return
        isClosing = true
        isProgressPaused = false
        isVerticalDragging = false
        onClose()
    }

    LaunchedEffect(startIndex, stories.size) {
        isClosing = false
        isProgressPaused = false
        isVerticalDragging = false
        lastTransitionTarget = pagerState.settledPage
        progress.snapTo(0f)
    }

    LaunchedEffect(pagerState.settledPage, stories) {
        stories.getOrNull(pagerState.settledPage)?.let { onStoryViewed(it.id) }
    }

    LaunchedEffect(pagerState.targetPage, pagerState.settledPage) {
        if (pagerState.targetPage != pagerState.settledPage && pagerState.targetPage != lastTransitionTarget) {
            lastTransitionTarget = pagerState.targetPage
            progress.snapTo(0f)
        }
        if (pagerState.targetPage == pagerState.settledPage) {
            lastTransitionTarget = pagerState.settledPage
        }
    }

    LaunchedEffect(pagerState.settledPage, isVerticalDragging, isProgressPaused, stories) {
        if (isVerticalDragging || isProgressPaused) return@LaunchedEffect
        val page = pagerState.settledPage
        val tickMs = 40L
        val increment = tickMs.toFloat() / storyDurationMs.toFloat()

        while (
            pagerState.settledPage == page &&
                !isVerticalDragging &&
                !isProgressPaused &&
                progress.value < 1f
        ) {
            delay(tickMs)
            if (
                pagerState.settledPage != page ||
                    isVerticalDragging ||
                    isProgressPaused
            ) {
                break
            }
            progress.snapTo((progress.value + increment).coerceAtMost(1f))
        }

        if (pagerState.settledPage == page && progress.value >= 1f) {
            if (page < stories.lastIndex) {
                pagerState.animateScrollToPage(page + 1)
            } else {
                closeViewer()
            }
        }
    }

    LaunchedEffect(pagerState.settledPage, progress.value, isVerticalDragging, isProgressPaused, stories.size) {
        if (
            stories.isNotEmpty() &&
            pagerState.settledPage == stories.lastIndex &&
            progress.value >= 0.999f &&
            !isVerticalDragging &&
            !isProgressPaused
        ) {
            closeViewer()
        }
    }

    if (stories.isEmpty()) return
    if (isClosing) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = (1f - (abs(verticalOffset.value) / (dismissThresholdPx * 2f)))
                        .coerceIn(0.15f, 1f),
                ),
            ),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = verticalOffset.value
                    val dragFraction = (abs(verticalOffset.value) / (dismissThresholdPx * 2f)).coerceIn(0f, 1f)
                    val scale = 1f - min(0.22f, dragFraction * 0.28f)
                    scaleX = scale
                    scaleY = scale
                    alpha = (1f - dragFraction).coerceIn(0.55f, 1f)
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            isVerticalDragging = true
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                verticalOffset.snapTo(verticalOffset.value + dragAmount)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (abs(verticalOffset.value) > dismissThresholdPx) {
                                    isVerticalDragging = false
                                    closeViewer()
                                } else {
                                    verticalOffset.animateTo(0f, tween(220, easing = FastOutSlowInEasing))
                                    isVerticalDragging = false
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                verticalOffset.animateTo(0f, tween(220, easing = FastOutSlowInEasing))
                                isVerticalDragging = false
                            }
                        },
                    )
                },
        ) { index ->
            val story = stories[index]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(index) {
                        detectTapGestures(
                            onPress = {
                                isProgressPaused = true
                                try {
                                    tryAwaitRelease()
                                } finally {
                                    isProgressPaused = false
                                }
                            },
                            onTap = { offset ->
                                val isStartTap = if (layoutDirection == LayoutDirection.Rtl) {
                                    offset.x > size.width * 0.8f
                                } else {
                                    offset.x < size.width * 0.2f
                                }
                                val isEndTap = if (layoutDirection == LayoutDirection.Rtl) {
                                    offset.x < size.width * 0.2f
                                } else {
                                    offset.x > size.width * 0.8f
                                }
                                when {
                                    isStartTap -> {
                                        val prev = (index - 1).coerceAtLeast(0)
                                        if (index > 0) {
                                            scope.launch {
                                                pagerState.animateScrollToPage(prev)
                                            }
                                        }
                                    }

                                    isEndTap -> {
                                        if (index >= stories.lastIndex) {
                                            closeViewer()
                                        } else {
                                            scope.launch {
                                                val next = (index + 1).coerceAtMost(stories.lastIndex)
                                                pagerState.animateScrollToPage(next)
                                            }
                                        }
                                    }
                                }
                            },
                        )
                    },
            ) {
                AsyncImage(
                    model = story.mediaUrl,
                    contentDescription = story.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                story.link?.let { link ->
                    val ctaLabel = link.title?.takeIf { it.isNotBlank() } ?: "Open"
                    TextButton(
                        onClick = { onLinkClick(link) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 56.dp),
                    ) {
                        Text(text = ctaLabel, color = Color.White)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
        ) {
            StoryProgressIndicators(
                total = stories.size,
                current = pagerState.currentPage,
                currentProgress = progress.value,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, end = 14.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = { closeViewer() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun StoryProgressIndicators(
    total: Int,
    current: Int,
    currentProgress: Float,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(total) { index ->
            val fill = when {
                index < current -> 1f
                index == current -> currentProgress.coerceIn(0f, 1f)
                else -> 0f
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.35f), shape = CircleShape),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fill)
                        .height(3.dp)
                        .background(Color.White, shape = CircleShape),
                )
            }
        }
    }
}
