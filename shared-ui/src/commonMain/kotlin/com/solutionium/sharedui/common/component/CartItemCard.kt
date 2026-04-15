package com.solutionium.sharedui.common.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.full_pay
import com.solutionium.sharedui.resources.installment_pay
import com.solutionium.shared.data.model.CartItem
import org.jetbrains.compose.resources.stringResource

@Composable
fun CartItemCard(
    modifier: Modifier = Modifier,
    cartItem: CartItem
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = cartItem.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Text(
                modifier = Modifier.weight(1f),
                text = cartItem.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "X",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                modifier = Modifier.padding(end = 4.dp),
                text = "${cartItem.quantity}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    validationMessage: String?,
    discountedPrice: (Double?) -> Double? = { null },
    showInstallmentPrice: Boolean = false,
    onProductClick: () -> Unit,
    onRemove: () -> Unit,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .clickable { onProductClick() },
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = cartItem.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(66.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {

                Text(
                    text = cartItem.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {


                    Column {
                        if (showInstallmentPrice) {
                            Row {
                                Text(
                                    text = stringResource(Res.string.installment_pay),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    " x 4 ",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                PriceView2(
                                    cartItem.currentPrice / 4,
                                    cartItem.isOnSale,
                                    cartItem.regularPrice?.let { it / 4 },
                                    magnifier = 1.15,

                                )
                            }
                            discountedPrice(cartItem.currentPrice)?.let {
                                Row {
                                    Text(
                                        text = stringResource(Res.string.full_pay),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    PriceView2(
                                        it,
                                        cartItem.isOnSale,
                                        cartItem.regularPrice,
                                        magnifier = 0.9,
                                    )
                                }
                            }
                        } else {
                            PriceView2(
                                cartItem.currentPrice,
                                cartItem.isOnSale,
                                cartItem.regularPrice,
                                magnifier = 1.05,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    CartIQuantityButtons(
                        quantity = cartItem.quantity,
                        maxQuantity = cartItem.currentStock ?: 12,
                        onIncreaseQuantity = onIncreaseQuantity,
                        onDecreaseQuantity = if (cartItem.quantity > 1) onDecreaseQuantity else onRemove,
                    )

                }


                if (cartItem.variationAttributes.any { it.slug != "pa_color" }) {
                    cartItem.variationAttributes.filter { it.slug != "pa_color" }
                        .forEach { attribute ->
                            Text(
                                text = "${attribute.name}: ${attribute.option}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                }

            }


        }

        // Validation Message Display
        if (cartItem.requiresAttention && validationMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            InfoBox(
                message = validationMessage,
                icon = Icons.Default.Warning,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
            )
        }
    }
}

@Composable
fun InfoBox(
    message: String,
    icon: ImageVector,
    color: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor)
        Text(text = message, color = contentColor, style = MaterialTheme.typography.bodyMedium)
    }
}
