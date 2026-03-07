package com.solutionium.sharedui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
        colors = CardDefaults.cardColors()
            .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = cartItem.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                modifier = Modifier.weight(1f),
                text = cartItem.name,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "X",
                fontSize = 12.sp,
                color = Color.LightGray
            )
            Text(
                modifier = Modifier.padding(end = 4.dp),
                text = "${cartItem.quantity}",
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    validationMessage: String?,
    discountedPrice: (Double?) -> Double? = { null },
    onProductClick: () -> Unit,
    onRemove: () -> Unit,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AsyncImage(
                model = cartItem.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clickable { onProductClick() },
                contentScale = ContentScale.Crop,

                )
            Column(
                modifier = Modifier
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = cartItem.name,
                    fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {


                    Column {
                        Row {
                            Text(
                                text = stringResource(Res.string.installment_pay),
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                " x 4 ",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            PriceView2(
                                cartItem.currentPrice / 4,
                                cartItem.isOnSale,
                                cartItem.regularPrice?.let { it / 4 }
                            )
                        }
                        discountedPrice(cartItem.currentPrice)?.let {
                            Row {
                                Text(
                                    text = stringResource(Res.string.full_pay),
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                PriceView2(
                                    it,
                                    cartItem.isOnSale,
                                    cartItem.regularPrice,
                                    magnifier = 0.8
                                )
                            }
                        }
                    }
//

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
                                color = Color.DarkGray
                            )
                        }
                }

            }


        }

        // Validation Message Display
        if (cartItem.requiresAttention && validationMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            InfoBox(
                message = validationMessage,
                icon = Icons.Default.Warning,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
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
            .background(color, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = message, color = contentColor, style = MaterialTheme.typography.bodyMedium)
    }
}
