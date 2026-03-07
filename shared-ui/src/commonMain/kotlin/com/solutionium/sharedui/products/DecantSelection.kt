package com.solutionium.sharedui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solutionium.shared.data.model.Decant
import com.solutionium.sharedui.common.component.FormattedPriceV3
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.decant_selection_title
import com.solutionium.sharedui.resources.full_bottle
import com.solutionium.sharedui.resources.out_of_stock
import org.jetbrains.compose.resources.stringResource

@Composable
fun DecantSelectionSection(
    productPrice: Double,
    decants: List<Decant>,
    selectedDecant: Decant?,
    onDecantSelected: (Decant) -> Unit,
    fullBottleAvailable: Boolean = true,
    onFullBottleSelected: () -> Unit,
    displayPrice: Boolean = false,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.decant_selection_title),
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.SemiBold,
            )
            val selectionText = selectedDecant?.size ?: stringResource(Res.string.full_bottle)
            Text(
                text = ": $selectionText",
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OptionChipWithPrice(
                primaryText = stringResource(Res.string.full_bottle),
                isAvailable = fullBottleAvailable,
                price = productPrice,
                isSelected = (selectedDecant == null && fullBottleAvailable),
                onClick = onFullBottleSelected,
                displayPrice = displayPrice,
            )

            decants.forEach { decant ->
                OptionChipWithPrice(
                    primaryText = decant.size,
                    price = decant.price,
                    isSelected = selectedDecant == decant,
                    onClick = { onDecantSelected(decant) },
                    displayPrice = displayPrice,
                )
            }
        }
    }
}

@Composable
private fun OptionChipWithPrice(
    primaryText: String,
    price: Double,
    isSelected: Boolean,
    isAvailable: Boolean = true,
    onClick: () -> Unit,
    displayPrice: Boolean = true,
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val alpha = if (isAvailable) 1f else 0.4f

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .alpha(alpha)
            .clickable(enabled = isAvailable, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = primaryText,
                color = contentColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            if (isAvailable) {
                if (displayPrice) {
                    FormattedPriceV3(
                        amount = price.toLong(),
                        mainStyle = TextStyle(
                            color = contentColor,
                            fontSize = 14.sp,
                        ),
                        magnifier = 0.8,
                    )
                }
            } else {
                Text(
                    text = stringResource(Res.string.out_of_stock),
                    color = contentColor,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
