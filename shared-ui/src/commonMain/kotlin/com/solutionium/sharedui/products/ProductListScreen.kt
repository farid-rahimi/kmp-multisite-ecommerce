package com.solutionium.sharedui.products

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.solutionium.shared.data.model.ProductThumbnail
import com.solutionium.sharedui.common.component.ProductThumbnailCard2
import com.solutionium.sharedui.common.component.ProductThumbnailPlaceholder
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun ProductListScreen(
    onProductClick: (id: Int) -> Unit,
    onBack: () -> Unit,
    args: Map<String, String> = emptyMap(),
) {
    val viewModel = koinInject<com.solutionium.shared.viewmodel.ProductListViewModel>(
        parameters = { parametersOf(args) }
    )
    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }

    val pagedList = viewModel.pagedList.collectAsLazyPagingItems()
    val state by viewModel.state.collectAsState()

    val isRefreshing = pagedList.loadState.refresh is LoadState.Loading

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { pagedList.refresh() } // <-- Call refresh on the Paging items
    ) {
        ProductListScreen(
            pagedList = pagedList,
            onProductClick = onProductClick,
            onBack = onBack,
            title = state.title,
            toggleFavorite = viewModel::toggleFavorite,
            discountedPrice = state::discountedPrice,
            isFavorite = state::isFavorite,
            cartCounter = state::cartItemCount,
            onAddToCartClick = viewModel::addToCart,
            onRemoveFromCartClick = viewModel::removeFromCart,
            showStock = state.isSuperUser,
            installmentPriceEnabled = state.installmentPriceEnabled
        )
    }
}

private operator fun PaddingValues.plus(other: PaddingValues): PaddingValues = PaddingValues(
    start = this.calculateStartPadding(LayoutDirection.Ltr) + other.calculateStartPadding(LayoutDirection.Ltr),
    top = this.calculateTopPadding() + other.calculateTopPadding(),
    end = this.calculateEndPadding(LayoutDirection.Ltr) + other.calculateEndPadding(LayoutDirection.Ltr),
    bottom = this.calculateBottomPadding() + other.calculateBottomPadding(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductListScreen(
    pagedList: LazyPagingItems<ProductThumbnail>,
    onProductClick: (id: Int) -> Unit,
    onBack: () -> Unit,
    title: String?,
    toggleFavorite: (Int, Boolean) -> Unit = { _, _ -> },
    discountedPrice: (Double?) -> Double? = { null },
    isFavorite: (Int) -> Boolean = { false },
    cartCounter: (Int) -> Int = { 0 },
    onAddToCartClick: (ProductThumbnail) -> Unit = {},
    onRemoveFromCartClick: (Int) -> Unit = {},
    showStock: Boolean = false,
    installmentPriceEnabled: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title ?: "Products") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            ProductListContent(
                contentPadding = padding,
                pagedList = pagedList,
                onProductClick = onProductClick,
                toggleFavorite = toggleFavorite,
                discountedPrice = discountedPrice,
                isFavorite = isFavorite,
                cartCounter = cartCounter,
                onAddToCartClick = onAddToCartClick,
                onRemoveFromCartClick = onRemoveFromCartClick,
                showStock = showStock,
                installmentPriceEnabled = installmentPriceEnabled
            )
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProductListContent(
    contentPadding: PaddingValues,
    pagedList: LazyPagingItems<ProductThumbnail>,
    onProductClick: (id: Int) -> Unit,
    toggleFavorite: (Int, Boolean) -> Unit = { _, _ -> },
    discountedPrice: (Double?) -> Double? = { null },
    isFavorite: (Int) -> Boolean = { false },
    cartCounter: (Int) -> Int = { 0 },
    onAddToCartClick: (ProductThumbnail) -> Unit = {},
    onRemoveFromCartClick: (Int) -> Unit = {},
    showStock: Boolean = false,
    installmentPriceEnabled: Boolean = false
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier,
        contentPadding = contentPadding + PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 1. Handle the initial loading state (full screen shimmer)
        if (pagedList.loadState.refresh is LoadState.Loading) {
            items(6) { // Show 6 placeholders for initial load
                ProductThumbnailPlaceholder(
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                )
            }
        }
        items(
            count = pagedList.itemCount,
            key = { index -> pagedList.peek(index)?.id ?: index },
        ) { index ->
            val item = pagedList[index]
            if (item != null) {

                ProductThumbnailCard2(
                    product = item,
                    onProductClick = { onProductClick(item.id) },
                    isFavorite = isFavorite(item.id),
                    onFavoriteClick = toggleFavorite,
                    modifier = Modifier
                        .animateItem(fadeInSpec = null, fadeOutSpec = null)
                        .width(200.dp),
                    discountedPrice = discountedPrice,
                    inCartQuantity = cartCounter(item.id),
                    maxQuantity = if (item.manageStock) item.stock else 12,
                    onAddToCartClick = { onAddToCartClick(item) },
                    onRemoveFromCartClick = onRemoveFromCartClick,
                    showStock = showStock,
                    showInstallmentPrice = installmentPriceEnabled

                )
            }
        }

        // 3. Handle the pagination loading state (shimmer at the bottom)
        if (pagedList.loadState.append is LoadState.Loading) {
            items(2) { // Show 2 placeholders for pagination
                ProductThumbnailPlaceholder(
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                )
            }
        }


    }
}
