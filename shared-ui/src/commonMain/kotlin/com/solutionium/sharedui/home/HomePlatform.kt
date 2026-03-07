package com.solutionium.sharedui.home

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import coil3.compose.AsyncImage
import com.solutionium.shared.data.model.BannerItem
import com.solutionium.shared.data.model.ContactInfo
import com.solutionium.shared.data.model.Link
import com.solutionium.shared.data.model.StoryItem
import com.solutionium.sharedui.common.component.BannerSlider
import kotlinx.coroutines.launch

@Composable
fun PlatformHeaderLogo(
    url: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

        AsyncImage(
            model = url,
            contentDescription = "Logo",
            modifier = modifier,
        )
    }

}

@Composable
fun PlatformBannerSlider(
    modifier: Modifier = Modifier,
    items: List<BannerItem>,
    onBannerClick: (item: BannerItem) -> Unit,
) {
    BannerSlider(
        modifier = modifier,
        items = items,
        onBannerClick = onBannerClick,
    )
}

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
fun PlatformContactSupportDialog(
    contactInfo: ContactInfo?,
    onDismiss: () -> Unit,
) {
    if (contactInfo == null) return

    val uriHandler = LocalUriHandler.current
    val contactRows = listOf(
        "WhatsApp" to contactInfo.whatsapp,
        "Telegram" to contactInfo.telegram,
        "Instagram" to contactInfo.instagram,
        "Call Us" to if (contactInfo.call.isNotBlank()) "tel:${contactInfo.call}" else "",
        "Email" to if (contactInfo.email.isNotBlank()) "mailto:${contactInfo.email}" else "",
    ).filter { it.second.isNotBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Contact Support") },
        text = {
            Column {
                contactRows.forEach { (label, url) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                runCatching { uriHandler.openUri(url) }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
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
    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, (stories.lastIndex).coerceAtLeast(0)),
        pageCount = { stories.size },
    )

    LaunchedEffect(pagerState.currentPage, stories) {
        stories.getOrNull(pagerState.currentPage)?.let { onStoryViewed(it.id) }
    }

    if (stories.isEmpty()) return

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Black,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, end = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { index ->
                val story = stories[index]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(index) {
                            detectTapGestures(
                                onTap = { offset ->
                                    when {
                                        offset.x < size.width * 0.2f -> {
                                            if (pagerState.currentPage > 0) {
                                                scope.launch {
                                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                                }
                                            }
                                        }

                                        offset.x > size.width * 0.8f -> {
                                            if (pagerState.currentPage < stories.lastIndex) {
                                                scope.launch {
                                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                                }
                                            } else {
                                                onClose()
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
                                .padding(bottom = 24.dp),
                        ) {
                            Text(text = ctaLabel, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
