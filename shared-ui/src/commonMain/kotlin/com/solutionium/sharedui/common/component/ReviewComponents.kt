package com.solutionium.sharedui.common.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solutionium.shared.data.model.Review
import com.solutionium.shared.data.model.ReviewChild
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.featured
import com.solutionium.sharedui.resources.helpful
import com.solutionium.sharedui.resources.helpful_votes
import com.solutionium.sharedui.resources.shop_manager
import com.solutionium.sharedui.resources.verified_buyer
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReviewItem(review: Review, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ReviewerAvatar(reviewerName = review.reviewer)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = review.reviewer,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = relativeTimeFromIso(review.dateCreated),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RatingBar(rating = review.rating, modifier = Modifier.padding(top = 2.dp))
                ReviewBadges(review = review)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.Top) {
            Text(
                modifier = Modifier.padding(horizontal = 6.dp),
                text = review.review.stripHtmlTags(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (review.criteriaRatings.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                review.criteriaRatings.forEach { criteria ->
                    CriteriaRatingBar(label = criteria.label, value = criteria.value)
                }
            }
        }

        if (review.children.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            review.children.forEach { response ->
                AdminResponseItem(response = response)
            }
        }
    }
}

@Composable
private fun ReviewBadges(review: Review, modifier: Modifier = Modifier) {
    val chips = mutableListOf<String>()
    if (review.featured) {
        chips += stringResource(Res.string.featured)
    }
    if (review.verified) {
        chips += stringResource(Res.string.verified_buyer)
    }
    if (review.helpful || review.helpfulVotes > 0) {
        chips += if (review.helpfulVotes > 0) {
            stringResource(Res.string.helpful_votes, review.helpfulVotes)
        } else {
            stringResource(Res.string.helpful)
        }
    }

    if (chips.isEmpty()) {
        return
    }

    Row(
        modifier = modifier.padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        chips.forEach { label ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AdminResponseItem(response: ReviewChild, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Reply,
            contentDescription = "Admin Reply",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(end = 12.dp, top = 4.dp)
                .size(20.dp)
        )

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.shop_manager, response.author),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = relativeTimeFromIso(response.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = response.content.stripHtmlTags(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun CriteriaRatingBar(
    label: String,
    value: Int,
    max: Int = 5,
    isEditable: Boolean = false,
    onEditRating: (Int) -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) value.toFloat() / max else 0f,
        label = "criteriaProgress"
    )

    LaunchedEffect(Unit) { isVisible = true }

    Column {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                color = Color.Gray,
                trackColor = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(CircleShape),
                strokeCap = StrokeCap.Round,
            )
            if (!isEditable) {
                Spacer(Modifier.width(12.dp))
                Text(
                    "$value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (isEditable) {
            Row {
                (1..5).forEach { index ->
                    Icon(
                        imageVector = if (index <= value) Icons.Filled.Star else Icons.Filled.StarOutline,
                        contentDescription = null,
                        tint = if (index <= value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onEditRating(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewerAvatar(reviewerName: String, modifier: Modifier = Modifier) {
    val initial = reviewerName.firstOrNull()?.uppercaseChar() ?: '#'
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
fun RatingBar(rating: Int, modifier: Modifier = Modifier, starColor: Color = Color(0xFFFFC107)) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (index < rating) starColor else Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ReviewSummaryItemCard(review: Review, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val initial = review.reviewer.firstOrNull()?.uppercaseChar() ?: '#'
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initial.toString(), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = review.reviewer,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (index < review.rating) Color(0xFFFFC107) else Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            ReviewBadges(
                review = review,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.review.stripHtmlTags(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
                modifier = Modifier.heightIn(min = 60.dp)
            )
        }
    }
}

private fun String.stripHtmlTags(): String = replace(Regex("<.*?>"), "")
