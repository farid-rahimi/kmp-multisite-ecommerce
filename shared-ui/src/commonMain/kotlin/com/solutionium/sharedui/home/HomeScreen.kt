package com.solutionium.sharedui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.solutionium.sharedui.common.component.ProductCarouselPlaceholder
import com.solutionium.sharedui.common.component.ProductThumbnailCard2
import com.solutionium.sharedui.common.component.StoryReelPlaceholder
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.app_offers
import com.solutionium.sharedui.resources.featured
import com.solutionium.sharedui.resources.new_arrivals
import com.solutionium.sharedui.resources.on_sales
import com.solutionium.shared.data.model.PRODUCT_ARG_FEATURED
import com.solutionium.shared.data.model.PRODUCT_ARG_ON_SALE
import com.solutionium.shared.data.model.PRODUCT_ARG_TAG
import com.solutionium.shared.data.model.PRODUCT_ARG_TITLE
import com.solutionium.shared.data.model.ProductListType
import com.solutionium.shared.data.model.ProductThumbnail
import com.solutionium.shared.data.model.StoryItem
import com.solutionium.shared.viewmodel.HomeViewModel
import com.solutionium.shared.viewmodel.UpdateType
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    onProductClick: (Int) -> Unit,
    navigateToProductList: (params: Map<String, String>) -> Unit = {},
    onStoryClick: (StoryItem) -> Unit,
    viewModel: HomeViewModel,
    onUpdateNowClick: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val bannerState by viewModel.bannerState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.headerLogoUrl?.let {
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        PlatformHeaderLogo(it, Modifier.height(32.dp))
                    }
                }

                if (state.storyItems.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        if (state.storiesLoading) {
                            StoryReelPlaceholder()
                        } else if (state.storyItems.isNotEmpty()) {
                            PlatformStoryReelSection(
                                stories = state.storyItems,
                                onStoryClick = onStoryClick,
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }

                item {
                    if (bannerState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        )
                    } else {
                        PlatformBannerSlider(
                            items = bannerState.banners,
                            onBannerClick = { banner ->
                                banner.link?.let { viewModel.onLinkClick(it) }
                            },
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }

                item {
                    if (state.newArrivalsLoading) {
                        ProductCarouselPlaceholder()
                    } else {
                        val title = stringResource(Res.string.new_arrivals)
                        ProductSectionRow(
                            title = title,
                            items = state.newArrivals,
                            installmentPriceEnabled = state.installmentPriceEnabled,
                            toggleFavorite = viewModel::toggleFavorite,
                            isFavorite = state::isFavorite,
                            discountedPrice = state::discountedPrice,
                            cartCounter = state::cartItemCount,
                            onProductClick = onProductClick,
                            onShowMoreProduceClick = {
                                navigateToProductList(mapOf(PRODUCT_ARG_TITLE to title))
                            },
                            onAddToCartClick = viewModel::addToCart,
                            onRemoveFromCartClick = viewModel::removeFromCart,
                            showStock = state.isSuperUser,
                        )
                    }
                }

                item {
                    if (state.appOffersLoading) {
                        ProductCarouselPlaceholder()
                    } else if (state.appOffers.isNotEmpty()) {
                        val title = stringResource(Res.string.app_offers)
                        ProductSectionRow(
                            title = title,
                            items = state.appOffers,
                            installmentPriceEnabled = state.installmentPriceEnabled,
                            toggleFavorite = viewModel::toggleFavorite,
                            isFavorite = state::isFavorite,
                            discountedPrice = state::discountedPrice,
                            cartCounter = state::cartItemCount,
                            onProductClick = onProductClick,
                            onShowMoreProduceClick = {
                                navigateToProductList(
                                    mapOf(
                                        PRODUCT_ARG_TITLE to title,
                                        PRODUCT_ARG_TAG to (ProductListType.Offers.queries["tag"] ?: ""),
                                    ),
                                )
                            },
                            onAddToCartClick = viewModel::addToCart,
                            onRemoveFromCartClick = viewModel::removeFromCart,
                            showStock = state.isSuperUser,
                        )
                    }
                }

                item {
                    if (state.onSalesLoading) {
                        ProductCarouselPlaceholder()
                    } else if (state.onSales.isNotEmpty()) {
                        val title = stringResource(Res.string.on_sales)
                        ProductSectionRow(
                            title = title,
                            items = state.onSales,
                            installmentPriceEnabled = state.installmentPriceEnabled,
                            toggleFavorite = viewModel::toggleFavorite,
                            isFavorite = state::isFavorite,
                            discountedPrice = state::discountedPrice,
                            cartCounter = state::cartItemCount,
                            onProductClick = onProductClick,
                            onShowMoreProduceClick = {
                                navigateToProductList(
                                    mapOf(
                                        PRODUCT_ARG_TITLE to title,
                                        PRODUCT_ARG_ON_SALE to "true",
                                    ),
                                )
                            },
                            onAddToCartClick = viewModel::addToCart,
                            onRemoveFromCartClick = viewModel::removeFromCart,
                            showStock = state.isSuperUser,
                        )
                    }
                }

                item {
                    if (state.featuredLoading) {
                        ProductCarouselPlaceholder()
                    } else if (state.featured.isNotEmpty()) {
                        val title = stringResource(Res.string.featured)
                        ProductSectionRow(
                            title = title,
                            items = state.featured,
                            installmentPriceEnabled = state.installmentPriceEnabled,
                            toggleFavorite = viewModel::toggleFavorite,
                            isFavorite = state::isFavorite,
                            discountedPrice = state::discountedPrice,
                            cartCounter = state::cartItemCount,
                            onProductClick = onProductClick,
                            onShowMoreProduceClick = {
                                navigateToProductList(
                                    mapOf(
                                        PRODUCT_ARG_TITLE to title,
                                        PRODUCT_ARG_FEATURED to "true",
                                    ),
                                )
                            },
                            onAddToCartClick = viewModel::addToCart,
                            onRemoveFromCartClick = viewModel::removeFromCart,
                            showStock = state.isSuperUser,
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }

        val updateInfo = state.updateInfo
        val isForcedUpdate = updateInfo.type == UpdateType.FORCED

        AnimatedVisibility(
            visible = isForcedUpdate,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ForcedUpdateScrim()
        }

        if (state.updateInfo.type != UpdateType.NONE) {
            UpdateDialog(
                updateInfo = state.updateInfo,
                onDismiss = { viewModel.dismissUpdateDialog() },
                onContactSupportClick = { viewModel.showContactSupport() },
                onUpdateNowClick = onUpdateNowClick,
            )
        }

        if (state.showContactSupportDialog && state.contactInfo != null) {
            PlatformContactSupportDialog(
                contactInfo = state.contactInfo,
                onDismiss = { viewModel.dismissContactSupport() },
            )
        }
    }
}

@Composable
fun ProductSectionRow(
    title: String,
    items: List<ProductThumbnail>,
    installmentPriceEnabled: Boolean = false,
    toggleFavorite: (Int, Boolean) -> Unit = { _, _ -> },
    discountedPrice: (Double?) -> Double? = { null },
    isFavorite: (Int) -> Boolean = { false },
    cartCounter: (Int) -> Int = { 0 },
    onProductClick: (Int) -> Unit,
    onShowMoreProduceClick: () -> Unit,
    onAddToCartClick: (ProductThumbnail) -> Unit = {},
    onRemoveFromCartClick: (Int) -> Unit = {},
    showStock: Boolean = false,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowMoreProduceClick() },
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { onShowMoreProduceClick() }) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowRight,
                    contentDescription = "More",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .height(320.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items) { item ->
                Modifier.testTag("discover_carousel_item")

                ProductThumbnailCard2(
                    product = item,
                    onProductClick = { onProductClick(item.id) },
                    onFavoriteClick = toggleFavorite,
                    isFavorite = isFavorite(item.id),
                    discountedPrice = discountedPrice,
                    modifier = Modifier
                        .animateItem()
                        //.fillParentMaxHeight()
                        .aspectRatio(0.6f),
                    inCartQuantity = cartCounter(item.id),
                    maxQuantity = if (item.manageStock) item.stock else 12,
                    onAddToCartClick = { onAddToCartClick(item) },
                    onRemoveFromCartClick = onRemoveFromCartClick,
                    priceMagnifier = 1.0,
                    showStock = showStock,
                    showInstallmentPrice = installmentPriceEnabled,
                )
            }
        }
    }
}

@Composable
private fun ForcedUpdateScrim(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.8f))
            .clickable(
                enabled = true,
                onClick = {},
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ),
    )
}
