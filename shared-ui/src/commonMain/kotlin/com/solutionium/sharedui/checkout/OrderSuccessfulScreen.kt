package com.solutionium.sharedui.checkout

import com.solutionium.sharedui.resources.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.solutionium.sharedui.common.component.FormattedPriceV3
import kotlin.math.max

// In a new file, e.g., OrderSuccessfulScreen.kt
@Composable
fun OrderSuccessfulScreen(
    orderId: Int,
    orderTotal: String,
    orderSubtotal: String,
    orderDiscount: String,
    vatRate: Double,
    onContinueShopping: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // You can replace this with a Lottie animation of a checkmark
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Success",
            tint = Color(0xFF00C853), // A nice green color
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.thank_you),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.order_successfull_msg),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Order Details Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(Res.string.order_number), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "#$orderId",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(Res.string.subtotal), style = MaterialTheme.typography.bodyLarge)
                    PriceWithVatNote(
                        amount = orderSubtotal.toDoubleOrNull() ?: 0.0,
                        vatRate = vatRate,
                        showVatNote = false,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if ((orderDiscount.toDoubleOrNull() ?: 0.0) > 0.0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(Res.string.discounts), style = MaterialTheme.typography.bodyLarge)
                        PriceWithVatNote(
                            amount = orderDiscount.toDoubleOrNull() ?: 0.0,
                            vatRate = vatRate,
                            amountColor = Color(0xFF0A7F52),
                            showVatNote = false,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(Res.string.total_paid),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    PriceWithVatNote(
                        amount = orderTotal.toDoubleOrNull() ?: 0.0,
                        vatRate = vatRate,
                        amountColor = MaterialTheme.colorScheme.primary,
                        isEmphasis = true,
                        showVatNote = true,
                    )
//                    Text(
//                        "€$orderTotal",
//                        style = MaterialTheme.typography.bodyLarge,
//                        fontWeight = FontWeight.SemiBold
//                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom

        Button(
            onClick = onContinueShopping,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(stringResource(Res.string.continue_shopping))
        }
    }
}

@Composable
private fun PriceWithVatNote(
    amount: Double,
    vatRate: Double,
    amountColor: Color = MaterialTheme.colorScheme.onSurface,
    isEmphasis: Boolean = false,
    showVatNote: Boolean = false,
) {
    val vatAmount = calculateVatFromGrossAmount(amount, vatRate)
    Column(horizontalAlignment = Alignment.End) {
        FormattedPriceV3(
            amount = amount,
            mainStyle = if (isEmphasis) {
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor,
                )
            } else {
                MaterialTheme.typography.bodyLarge.copy(color = amountColor)
            },
        )
        if (showVatNote && vatAmount > 0.0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${stringResource(Res.string.includes_vat)}: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FormattedPriceV3(
                    amount = vatAmount,
                    mainStyle = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

private fun calculateVatFromGrossAmount(amount: Double, vatRate: Double): Double {
    if (amount <= 0.0 || vatRate <= 0.0) return 0.0
    val normalizedRate = if (vatRate > 1.0) vatRate / 100.0 else vatRate
    val net = amount / (1.0 + normalizedRate)
    return max(amount - net, 0.0)
}
