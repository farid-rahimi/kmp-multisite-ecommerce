package com.solutionium.sharedui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun CartIQuantityButtons(
    quantity: Int,
    maxQuantity: Int,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerShape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier

            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                shape = containerShape,
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                shape = containerShape,
            ).padding(horizontal = 4.dp, vertical = 2.dp)
            .clip(containerShape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (quantity <= 1) {
            IconButton(
                onClick = onDecreaseQuantity,
                modifier = Modifier.size(30.dp),
            ) {
                Icon(Icons.Outlined.Delete, tint = MaterialTheme.colorScheme.onSurfaceVariant, contentDescription = "Remove")
            }
        } else {
            IconButton(
                onClick = onDecreaseQuantity,
                modifier = Modifier.size(30.dp),
            ) {
                Icon(Icons.Filled.Remove, tint = MaterialTheme.colorScheme.onSurfaceVariant, contentDescription = "Decrease")
            }
        }

        Text(
            text = "$quantity",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
        IconButton(
            onClick = onIncreaseQuantity,
            enabled = quantity < maxQuantity,
            modifier = Modifier.size(30.dp),
        ) {
            val tint = if (quantity < maxQuantity) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            }
            Icon(Icons.Default.Add, tint = tint, contentDescription = "Increase")
        }
    }
}
