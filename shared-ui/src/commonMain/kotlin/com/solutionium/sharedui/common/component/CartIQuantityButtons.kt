package com.solutionium.sharedui.common.component

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CartIQuantityButtons(
    quantity: Int,
    maxQuantity: Int,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp)), // Ensures the content (like ripple effects) is clipped to the shape
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (quantity <= 1) {
            IconButton(
                onClick = onDecreaseQuantity,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Outlined.Delete, tint = Color.Gray, contentDescription = "Remove")
            }
        } else {
            IconButton(
                onClick = onDecreaseQuantity,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Filled.Remove, tint = Color.Gray, contentDescription = "Decrease")
            }
        }

        Text(
            text = "$quantity",
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        IconButton(
            onClick = onIncreaseQuantity,
            enabled = quantity < maxQuantity,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(Icons.Default.Add, tint = Color.Gray, contentDescription = "Increase")
        }
    }
}