package com.solutionium.sharedui.common.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ProductCarouselPlaceholder() {
    Column {
        // Title placeholder
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(24.dp)
                .padding(start = 16.dp)
                .shimmerPlaceholder()
        )
        LazyRow(contentPadding = PaddingValues(16.dp)) {
            items(3) {
                // Use the placeholder from your product list
                ProductThumbnailPlaceholder()
            }
        }
    }
}

@Composable
fun StoryReelPlaceholder() {
    Column {
        // Title placeholder
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(24.dp)
                .padding(start = 16.dp)
                .shimmerPlaceholder()
        )
        LazyRow(contentPadding = PaddingValues(16.dp)) {
            items(5) {
                // Circular story item placeholder
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).shimmerPlaceholder())
            }
        }
    }
}

@Composable
fun ProductThumbnailPlaceholder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(160.dp).padding(4.dp), // Match your real card's width
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shimmerPlaceholder()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Product Name Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .shimmerPlaceholder()
                )

                // Price Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .shimmerPlaceholder()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .shimmerPlaceholder()
                )
            }
        }
    }
}


@Composable
fun CategoryScreenPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Placeholder for a title
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(24.dp)
                .width(150.dp)
                .shimmerPlaceholder()
        )
        // Placeholder for a horizontal list (like brands)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .shimmerPlaceholder()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .height(16.dp)
                            .width(60.dp)
                            .shimmerPlaceholder()
                    )
                }
            }
        }

        // Placeholder for another title
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(24.dp)
                .width(200.dp)
                .shimmerPlaceholder()
        )
        // Placeholder for a grid-like section
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerPlaceholder()
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerPlaceholder()
            )
        }
    }
}


@Composable
fun OrderSummaryCardPlaceholder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Order ID and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(120.dp)
                        .shimmerPlaceholder()
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(80.dp)
                        .shimmerPlaceholder()
                )
            }

            // Middle Row: Item thumbnails
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .shimmerPlaceholder()
                    )
                }
            }

            // Bottom Row: Status and Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .width(90.dp)
                        .shimmerPlaceholder()
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(100.dp)
                        .shimmerPlaceholder()
                )
            }
        }
    }
}