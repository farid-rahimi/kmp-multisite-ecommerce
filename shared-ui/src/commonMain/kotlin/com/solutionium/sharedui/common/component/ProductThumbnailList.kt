package com.solutionium.sharedui.common.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.solutionium.shared.data.model.ProductThumbnail

@Composable
fun ProductThumbnailList(
    products: List<ProductThumbnail>,
    onProductClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(products, key = { it.id }) { product ->
            ProductThumbnailCard2(
                product = product,
                onProductClick = onProductClick,
                onFavoriteClick = { _, _ -> },
                onAddToCartClick = {},
                onRemoveFromCartClick = {},
                showStock = true,
            )
        }
    }
}
