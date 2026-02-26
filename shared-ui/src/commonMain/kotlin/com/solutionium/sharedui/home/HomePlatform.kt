package com.solutionium.sharedui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.solutionium.shared.data.model.BannerItem
import com.solutionium.shared.data.model.ContactInfo
import com.solutionium.shared.data.model.StoryItem
import com.solutionium.sharedui.common.component.BannerSlider

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
