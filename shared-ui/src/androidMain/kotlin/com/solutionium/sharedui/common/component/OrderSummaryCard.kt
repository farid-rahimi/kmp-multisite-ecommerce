package com.solutionium.sharedui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.solutionium.sharedui.common.DateHelper
import com.solutionium.core.ui.common.R
import com.solutionium.shared.data.model.Order

enum class OrderStatusFilter(val key: String, val titleResourceId: Int, val colorId: Int) {
    ALL("all", R.string.all, R.color.all),
    PROCESSING("processing", R.string.processing, R.color.processing),
    AWAITING("awaiting-shipment", R.string.awaiting, R.color.awaiting),
    ON_HOLD("on-hold", R.string.on_hold, R.color.on_hold),
    COMPLETED("completed", R.string.completed, R.color.completed),
    CANCELLED("cancelled", R.string.cancelled, R.color.cancelled),
    FAILED("failed", R.string.failed, R.color.failed),
    REFUNDED("refunded", R.string.refunded, R.color.refunded),
    PENDING("pending", R.string.pending, R.color.pending)
}

@Composable
fun OrderSummaryCard(
    order: Order,
    onClick: () -> Unit
) {
    val formattedDate = remember(order.dateCreated) {
        DateHelper.convertDateStringToJalali(order.datePaid ?: order.dateCreated)
    }

    val statusText = stringResource( OrderStatusFilter.entries.find { it.key == order.status }?.titleResourceId ?: R.string.unknown_order_status)
    val statusColor = colorResource( OrderStatusFilter.entries.find { it.key == order.status }?.colorId ?: R.color.unknown)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Order ID and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.order, order.id),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Middle Row: Item thumbnails
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically

            ) {
                order.lineItems.take(4).forEach { item ->
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = "Order Item",
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                MaterialTheme.shapes.small
                            )
                            .background(Color.White)
                            .padding(8.dp)
                            .size(50.dp),
                        contentScale = ContentScale.Inside
                    )
                }
                if (order.lineItems.size > 4) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${order.lineItems.size - 4}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row: Status and Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(statusColor)
                        .padding(8.dp)
                    ,
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                PriceView(order.total.toDouble(), false, null)

            }
        }
    }
}
