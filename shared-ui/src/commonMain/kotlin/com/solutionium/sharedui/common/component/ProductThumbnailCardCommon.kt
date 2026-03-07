package com.solutionium.sharedui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

/**
 * commonMain model for iOS/Android shared product card rendering.
 */
data class SharedProductThumbnail(
    val id: Int,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val isFavorite: Boolean = false,
    val inCartQuantity: Int = 0,
    val regularPrice: Double? = null,
    val onSale: Boolean = false,
    val salePercent: Int = 0,
    val stockCount: Int = 0,
    val stockStatus: String = "instock",
    val manageStock: Boolean = false,
    val featureText: String? = null,
)

@Composable
fun ProductThumbnailCard(
    product: SharedProductThumbnail,
    onProductClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteClick: (Int, Boolean) -> Unit = { _, _ -> },
    onAddToCartClick: (Int) -> Unit = {},
) {
    Card(
        onClick = { onProductClick(product.id) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .background(Color.White),
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                )

                IconButton(
                    onClick = { onFavoriteClick(product.id, product.isFavorite) },
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp),
                        ),
                ) {
                    Icon(
                        imageVector = if (product.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        tint = if (product.isFavorite) Color.Red.copy(alpha = 0.7f) else Color.Gray,
                    )
                }

                if (product.onSale && product.salePercent > 0) {
                    SalesBadge(
                        discount = product.salePercent,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                    )
                }

                IconButton(
                    onClick = { onAddToCartClick(product.id) },
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(4.dp),
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = product.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.heightIn(min = 30.dp),
                )

                val stockText = when {
                    product.manageStock && product.stockCount > 0 -> "In stock: ${product.stockCount}"
                    !product.manageStock && product.stockStatus == "instock" -> "In stock"
                    else -> "Out of stock"
                }
                Text(
                    text = stockText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (stockText.startsWith("In stock")) Color(0xFF4CAF50) else Color(0xFFF44336),
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Installment pay", fontSize = 9.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("x 4", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = formatPrice(product.price / 4.0),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Full pay", fontSize = 9.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = formatPrice(product.price),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                    )
                }

                product.featureText?.let {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}



@Composable
fun ProductThumbnailCardList(
    products: List<SharedProductThumbnail>,
    onProductClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(products, key = { it.id }) { product ->
            ProductThumbnailCard(
                product = product,
                onProductClick = onProductClick,
            )
        }
    }
}

private fun formatPrice(price: Double): String = ((price * 100).toInt() / 100.0).toString()
