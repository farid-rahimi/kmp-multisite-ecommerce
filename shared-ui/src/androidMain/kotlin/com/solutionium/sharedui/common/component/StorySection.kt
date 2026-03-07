package com.solutionium.sharedui.common.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.solutionium.core.ui.common.R
import com.solutionium.shared.data.model.Link
import com.solutionium.shared.data.model.StoryItem
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// -------------- 1. Story Reel Section for Home Screen --------------
@Composable
fun StoryReelSection(
    stories: List<StoryItem>,
    onStoryClick: (StoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(stories) { story ->
            StoryPreviewItem(
                story = story,
                onClick = { onStoryClick(story) }
            )
        }
    }
}

// -------------- 2. Circular Story Preview Item --------------
@Composable
fun StoryPreviewItem(
    story: StoryItem,
    onClick: () -> Unit
) {
    val ringColors = if (story.isViewed) {
        listOf(Color.LightGray, Color.LightGray)
    } else {
        listOf(Color(0xFF833AB4), Color(0xFFF77737), Color(0xFFE1306C))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick)
        ) {
            // Background ring
            Card(
                shape = CircleShape,
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.dp, Brush.verticalGradient(ringColors))
            ) {
                // Image with padding inside the ring
                AsyncImage(
                    model = story.mediaUrl,
                    contentDescription = story.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp)
                        .clip(CircleShape)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = story.title,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StoryProgressIndicator(
    modifier: Modifier = Modifier,
    storyCount: Int,
    currentStoryIndex: Int,
    isPaused: Boolean,
    onAnimationEnd: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        for (i in 0 until storyCount) {
            val isCurrentPage = i == currentStoryIndex
            var progress by remember(currentStoryIndex) {
                // If it's a previous page, it's full. If it's a future page, it's empty.
                mutableFloatStateOf(if (i < currentStoryIndex) 1f else 0f)
            }

            LaunchedEffect(isCurrentPage, isPaused) {
                if (isCurrentPage && !isPaused) {
                    coroutineScope {
                        // Animate from current progress to 1f
                        animate(
                            initialValue = progress,
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 5000, easing = LinearEasing) // 5-second story duration
                        ) { value, _ ->
                            progress = value
                        }
                    }
                    // When animation finishes, notify the parent to move to the next story
                    onAnimationEnd()
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = if (isCurrentPage) progress else if (i < currentStoryIndex) 1f else 0f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White)
                )
            }
            if (i < storyCount - 1) {
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            }
        }
    }
}


// -------------- 3. Full-Screen Story Viewer --------------
@Composable
fun StoryViewer(
    stories: List<StoryItem>,
    startIndex: Int,
    onClose: () -> Unit,
    onLinkClick: (Link) -> Unit,
    onStoryViewed: (storyId: Int) -> Unit,
    dismissThreshold: Float = 200f // The distance in pixels to trigger dismiss
) {

    BackHandler {
        onClose()
    }

    var offsetY by remember { mutableFloatStateOf(0f) }

    val pagerState = rememberPagerState(initialPage = startIndex)
    val scope = rememberCoroutineScope() // <-- 1. Get a coroutine scope

    var isPaused by remember { mutableStateOf(false) }

    // Mark the initial story as viewed
    LaunchedEffect(pagerState.currentPage) {
        onStoryViewed(stories[pagerState.currentPage].id)
    }

    val onNext: () -> Unit = {
        if (pagerState.currentPage < stories.lastIndex) {
            scope.launch { pagerState.animateScrollToPage(page = pagerState.currentPage + 1) }
        } else {
            onClose()
        }
    }

    val onPrevious = {
        if (pagerState.currentPage > 0) {
            scope.launch { pagerState.animateScrollToPage(page = pagerState.currentPage - 1) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        // When the user lifts their finger, check if the threshold was met
                        if (offsetY > dismissThreshold || offsetY < -dismissThreshold) {
                            onClose()
                        } else {
                            // If not, animate back to the center (optional, but good UX)
                            offsetY = 0f
                        }
                    }
                ) { _, dragAmount ->
                    // Update the offset as the user drags
                    offsetY += dragAmount
                }
            }
            // 3. Apply the vertical offset to the HorizontalPager
            .offset { IntOffset(0, offsetY.roundToInt()) }
    ) {
        HorizontalPager(
            count = stories.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            // Tap on the right 2/3 of the screen to go next, left 1/3 to go previous
                            if (offset.x > size.width * (4 / 5f)) {
                                onPrevious()
                            } else if (offset.x < size.width * (1 / 5f)) {
                                onNext()
                            }
                        },
                        onPress = {
                            // When user presses down, pause the animation
                            isPaused = true
                            tryAwaitRelease() // Wait until they lift their finger
                            // When they release, resume the animation
                            isPaused = false
                        }
                    )
                }
        ) { page ->
            StoryPage(
                story = stories[page],
                onLinkClick = onLinkClick
//                onNext = {
//                    if (page < stories.lastIndex) {
//                        // ▼▼▼ 2. Launch a coroutine to call the suspend function ▼▼▼
//                        scope.launch {
//                            pagerState.animateScrollToPage(page = page + 1)
//                        }
//                    } else {
//                        onClose()
//                    }
//                },
//                onPrevious = {
//                    if (page > 0) {
//                        // ▼▼▼ 3. Do the same for the previous action ▼▼▼
//                        scope.launch {
//                            pagerState.animateScrollToPage(page = page - 1)
//                        }
//                    }
//                },
                //onClose = onClose
            )
        }

        StoryProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter), // Position it at the top
            storyCount = stories.size,
            currentStoryIndex = pagerState.currentPage,
            isPaused = isPaused,
            onAnimationEnd = onNext // When a story finishes, go to the next one
        )

    }
}

// -------------- 4. Individual Story Page --------------
@Composable
fun StoryPage(
    story: StoryItem,
    modifier: Modifier = Modifier,
    onLinkClick: (Link) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // Add a background for cases where the image is loading
    ) {
        // Background Image/Video
        AsyncImage(
            model = story.mediaUrl,
            contentDescription = "Story content for ${story.title}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // You can add an overlay gradient for better text readability if needed
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent),
                        startY = 0f,
                        endY = 500f
                    )
                )
        )

        // Top UI (User info, etc. - shown on top of the gradient)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(top = 24.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // You could have a smaller profile picture here
            // AsyncImage(model = story.userProfilePic, ...)

            Text(
                text = story.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        story.link?.let {
            Button(
                onClick = { onLinkClick(it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = it.title ?: stringResource(R.string.show_more),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

