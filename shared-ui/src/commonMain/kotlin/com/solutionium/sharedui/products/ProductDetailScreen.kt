package com.solutionium.sharedui.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import coil3.compose.AsyncImage
import com.solutionium.sharedui.common.component.PriceView2
import com.solutionium.sharedui.common.component.ReviewSummaryItemCard
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.*
import com.solutionium.shared.data.model.PRODUCT_ARG_BRAND_ID
import com.solutionium.shared.data.model.PRODUCT_ARG_TITLE
import com.solutionium.shared.data.model.ProductAttribute
import com.solutionium.shared.data.model.ProductDetail
import com.solutionium.shared.data.model.ProductVarType
import com.solutionium.shared.data.model.Review
import com.solutionium.shared.data.model.SimpleTerm
import com.solutionium.shared.viewmodel.ProductDetailState
import com.solutionium.shared.viewmodel.ProductDetailViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.ranges.coerceIn


@Composable
fun CommentsSection(
    comments: List<Review>,
    averageRating: Double,
    reviewCount: Int,
    onSeeAllClick: () -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(Res.string.reviews_count, reviewCount),
                style = MaterialTheme.typography.titleSmall,
            )
            // Show "See All" only if there are reviews
            if (comments.isNotEmpty()) {
                TextButton(onClick = onSeeAllClick) {
                    Text(stringResource(Res.string.see_all_reviews))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Carousel of reviews
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(comments.take(4)) { review ->
                ReviewSummaryItemCard(review = review)
            }
        }

    }
}


@Composable
private fun isScrollingUp(scrollState: ScrollState): Boolean {
    var previousScrollOffset by remember(scrollState) { mutableStateOf(scrollState.value) }
    return remember(scrollState.value) {
        val isScrollingUp = scrollState.value < previousScrollOffset
        previousScrollOffset = scrollState.value
        isScrollingUp
    }
}

@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    onAllReviewClicked: (Int, List<Int>) -> Unit,
    navigateToProductList: (Map<String, String>) -> Unit = {},
    onBackClick: () -> Unit,
    onShareClick: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.state.collectAsState()

    val product = uiState.product

    // The UI collects the variation state directly from the ViewModel
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val selectedVariation by viewModel.selectedVariation.collectAsState()
    val productAttributes by viewModel.productVariationAttributes.collectAsState()
    val variations by viewModel.variations.collectAsState()
    // Now you can use `selectedVariation` to update the price, image, etc.
    val currentImage by viewModel.selectedImageUrl.collectAsState() //?: product?.imageUrls?.first()
    val currentPrice = selectedVariation?.price ?: product?.price
    val selectedDecant by viewModel.selectedDecant.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState() // <-- Collect the refreshing state



    val scrollState = rememberScrollState()

    var manualBottomBarVisible by remember { mutableStateOf(false) }

    // This effect will run whenever `selectedVariation` is not null and changes.
    LaunchedEffect(selectedDecant, selectedVariation, uiState.cartItem?.quantity) {
        //if (selectedVariation != null || selectedDecant != null) {
        manualBottomBarVisible = true
        // Hide the bar automatically after 5 seconds if the user doesn't interact
        delay(6000L)
        manualBottomBarVisible = false
        //}
    }

    val isBottomBarVisible =
        (isScrollingUp(scrollState) || scrollState.value == 0 || manualBottomBarVisible)
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                ProductDetailTopAppBar(
                    productName = uiState.product?.name,
                    isFavorite = uiState.isFavorite(),
                    scrollAlpha = (scrollState.value / 1200f).coerceIn(0f, 1f),
                    onBackClick = onBackClick,
                    onShareClick = {
                        onShareClick(
                            uiState.product?.name ?: "",
                            uiState.product?.permalink ?: "",
                        )
                    },
                    onFavoriteClick = { viewModel.toggleFavorite() },
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent

        ) { paddingValues ->
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize(),
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh() }
            ) {
                Crossfade(
                    targetState = when {
                        uiState.isLoading -> ScreenState.Loading
                        uiState.error != null -> ScreenState.Error
                        else -> ScreenState.Content
                    },
                    animationSpec = tween(300),
                    label = "ScreenStateCrossfade"
                ) { state ->
                    when (state) {
                        ScreenState.Loading -> LoadingIndicator(
                            Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        )

                        ScreenState.Error -> ErrorView(
                            errorMessage = uiState.error?.toString()
                                ?: stringResource(Res.string.an_unexpected_error_occurred),
                            onRetry = viewModel::onRetry,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(16.dp)
                        )

                        ScreenState.Content -> {
                            uiState.product?.let { product ->
                                ProductContent(
                                    product = product,
                                    currentVarImage = currentImage,
                                    uiState = uiState,
                                    scrollState = scrollState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = paddingValues.calculateBottomPadding()) // Only bottom for content
                                        .verticalScroll(scrollState),
                                    onBrandClick = { brand ->
                                        navigateToProductList(
                                            mapOf(
                                                PRODUCT_ARG_BRAND_ID to brand.id.toString(),
                                                PRODUCT_ARG_TITLE to brand.name,
                                            )
                                        )
                                    },
                                    decants = {
                                        product.decants.takeIf { it.isNotEmpty() && product.isAvailable }
                                            ?.let { decantOptions ->
                                                DecantSelectionSection(
                                                    productPrice = product.price, // Pass the base price for the "Full Bottle" option
                                                    decants = decantOptions,
                                                    selectedDecant = selectedDecant,
                                                    onDecantSelected = viewModel::onDecantSelected,
                                                    fullBottleAvailable = product.isInStock,
                                                    onFullBottleSelected = viewModel::onFullBottleSelected
                                                )
                                                Spacer(modifier = Modifier.height(20.dp))
                                            }
                                    },
                                    onAllReviewClicked = onAllReviewClicked,
                                    variations = {
                                        if (product.varType == ProductVarType.VARIABLE) {
                                            VariationSelectionSection(
                                                attributes = productAttributes,
                                                variations = variations,
                                                isLoading = uiState.isLoadingVariations,
                                                selectedOptions = selectedOptions,
                                                onOptionSelected = viewModel::onOptionSelected, // Pass the event handler
                                                isOptionAvailable = viewModel::isOptionAvailable // Pass the availability checker
                                            )
                                        }
                                    }
                                )
                            }
                                ?: ErrorView( // Fallback if product is null in content state (should ideally not happen)
                                    errorMessage = stringResource(Res.string.product_data_is_unavailable),
                                    onRetry = viewModel::onRetry,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(paddingValues)
                                        .padding(16.dp)
                                )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isBottomBarVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter) // <-- This is key
        ) {
            uiState.product?.let { product ->
                if (!uiState.isLoading && uiState.error == null) {
                    ProductDetailBottomBar(
                        price = selectedDecant?.price ?: selectedVariation?.price ?: product.price,
                        priceHtml = product.priceHtml,
                        onSales = selectedVariation?.onSale ?: product.onSale,
                        salesPrice = selectedVariation?.salePrice ?: product.salePrice,
                        regularPrice = selectedDecant?.regularPrice
                            ?: selectedVariation?.regularPrice ?: product.regularPrice,
                        discountedPrice = uiState::discountedPrice,
                        stockStatus = selectedVariation?.stockStatus ?: product.stockStatus,
                        isLowStock = selectedVariation?.lowInStock ?: product.lowInStock,
                        cartQuantity = uiState.cartItem?.quantity ?: 0,
                        maxQuantity = uiState.cartItem?.currentStock ?: 12,
                        isVariationSelected =
                                    (product.varType == ProductVarType.SIMPLE) ||
                                    (product.varType == ProductVarType.VARIABLE && selectedVariation != null) ||
                                    (selectedDecant != null),
                        showInstallmentPrice = uiState.installmentPriceEnabled,
                        onAddToCartClick = viewModel::onAddToCartClick,
                        onIncreaseItem = viewModel::increaseQuantity,
                        onRemove = viewModel::removeItem
                    )
                }
            }
        }
    }

}

private enum class ScreenState { Loading, Error, Content }

// --- Reusable UI Components ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailTopAppBar(
    productName: String?,
    isFavorite: Boolean,
    scrollAlpha: Float,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                productName ?: stringResource(Res.string.product_detail_title),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                //fontWeight = FontWeight.SemiBold,
                modifier = Modifier.alpha(if (scrollAlpha > 0.9f) (scrollAlpha - 0.9f) / 0.1f else 0f),
                //color = LocalContentColor.current.copy(alpha = scrollAlpha) // Fade the text in/out
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(
                onClick = onShareClick,
                modifier = Modifier
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Share")
            }
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
//                    .background(
//                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
//                        shape = RoundedCornerShape(4.dp)
//                    ),
            ) {
                Icon(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red.copy(alpha = 0.7f) else Color.Gray
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = scrollAlpha)
        )
    )
}

@Composable
private fun ProductContent(
    product: ProductDetail,
    currentVarImage: String?,
    uiState: ProductDetailState,
    scrollState: ScrollState,
    onAllReviewClicked: (Int, List<Int>) -> Unit,
    modifier: Modifier = Modifier,
    onBrandClick: (SimpleTerm) -> Unit = {},
    decants: @Composable () -> Unit = { },
    variations: @Composable () -> Unit = { },
) {
    var showFullDescription by remember(product.id) { mutableStateOf(false) }
    var showFullInspiration by remember(product.id) { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(true) }

    Column(modifier = modifier) {
        ProductImageHeader(
            imageUrls = product.imageUrls,
            currentVarImage = currentVarImage,
            productName = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 24.dp, bottom = 8.dp)
                .heightIn(min = 400.dp, max = 400.dp)
                .graphicsLayer { // Parallax effect
                    alpha = 1f - (scrollState.value / 1200f).coerceIn(
                        0.0f,
                        if (uiState.isVariable) 0.0f else 0.9f
                    )
                    translationY = scrollState.value * 0.4f
                }
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
            ProductBasicInfo(product, onBrandClick)
            //Spacer(modifier = Modifier.height(16.dp))
        }

        decants()

        variations()

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            ProductRatingSummary(
                averageRating = uiState.averageRating ?: 0f,
                reviewCount = uiState.comments.size,
                onClick = {  }
            )
            Spacer(modifier = Modifier.height(20.dp))

            ProductAttributesInfo(
                attributes = product.attributes.filter { it.visible },
                countryOfOrigin = product.brands.first().name // Assuming brand name as origin for simplicity
            )
            Spacer(modifier = Modifier.height(20.dp))

            CollapsibleSection(
                title = stringResource(Res.string.description),
                htmlContent = product.description,
                isExpanded = showFullDescription,
                onToggle = { showFullDescription = !showFullDescription }
            )
            Spacer(modifier = Modifier.height(20.dp))

            product.inspiration?.takeIf { it.isNotBlank() }?.let {
                CollapsibleSection(
                    title = "The Inspiration",
                    htmlContent = it,
                    isExpanded = showFullInspiration,
                    onToggle = { showFullInspiration = !showFullInspiration }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

//            ProductNotesInfo(
//                topNotes = product.topNotes,
//                heartNotes = product.heartNotes,
//                baseNotes = product.baseNotes
//            )
//            Spacer(modifier = Modifier.height(24.dp))


            product.usageSuggestion?.takeIf { it.isNotBlank() }?.let {
                InfoPill(icon = Icons.Outlined.Info, text = it)
                Spacer(modifier = Modifier.height(10.dp))
            }
//            InfoPill(
//                icon = Icons.Filled.LocalShipping,
//                text = "Estimated Delivery: 2-4 business days"
//            )
//            Spacer(modifier = Modifier.height(10.dp))
//            InfoPill(icon = Icons.Outlined.Policy, text = "Authenticity Guaranteed & Easy Returns")
//            Spacer(modifier = Modifier.height(24.dp))


        }

        if (uiState.comments.isNotEmpty()) { // Always show review section for now
            Text(
                stringResource(Res.string.customer_reviews_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            CommentsSection(
                comments = uiState.comments.take(4),
                averageRating = product.rating,
                reviewCount = product.ratingCount,
                onSeeAllClick = {
                    onAllReviewClicked(
                        product.id,
                        product.categories.map { it.id })
                },
            )
        } else {
            NoReviewsPrompt(
                productName = product.name,
                onWriteReviewClick = {
                    onAllReviewClicked(
                        product.id,
                        product.categories.map { it.id })
                })
        }
        Spacer(modifier = Modifier.height(80.dp)) // Space for bottom bar
    }
}

@Composable
private fun ProductImageHeader(
    imageUrls: List<String>,
    currentVarImage: String?,
    productName: String,
    modifier: Modifier = Modifier
) {

    val displayImages = if (currentVarImage != null) {
        listOf(currentVarImage)
    } else {
        imageUrls
    }
    val pagerState = rememberPagerState(pageCount = { displayImages.size })

    //var isZoomed by remember { mutableStateOf(false) }

    Box (
        modifier = modifier
    ) {
        if (displayImages.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                //userScrollEnabled = !isZoomed,
                modifier = Modifier.fillMaxSize() //.weight(1f)
            ) { page ->
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                // Reset zoom state when the page changes
//                LaunchedEffect(pagerState.currentPage) {
//                    if (pagerState.currentPage != page) {
//                        scale = 1f
//                        offset = Offset.Zero
//                        isZoomed = false
//                    }
//                }
                LaunchedEffect(pagerState.isScrollInProgress) {
                    if (!pagerState.isScrollInProgress && pagerState.currentPage != page) {
                        scale = 1f
                        offset = Offset.Zero
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                //awaitPointerEventScope {
                                    // Wait for a finger to touch the screen
                                    awaitFirstDown()
                                    do {
                                        val event = awaitPointerEvent()
                                        val zoom = event.calculateZoom()
                                        val pan = event.calculatePan()

                                        // Update scale based on zoom gesture
                                        scale = (scale * zoom).coerceIn(1f, 3f)

                                        // Only allow panning if the image is zoomed in.
                                        if (scale > 1f) {
                                            val newOffset = offset + pan
                                            // Bounds checking to prevent panning too far
                                            val maxX = (size.width * (scale - 1)) / 2
                                            val maxY = (size.height * (scale - 1)) / 2
                                            offset = Offset(
                                                x = newOffset.x.coerceIn(-maxX, maxX),
                                                y = newOffset.y.coerceIn(-maxY, maxY)
                                            )
                                            // If we are zoomed, consume the events so the pager doesn't scroll
                                            event.changes.forEach {
                                                if (it.positionChanged()) {
                                                    it.consume()
                                                }
                                            }
                                        }
                                        // If scale is 1f, we don't consume, letting the pager handle the swipe.

                                    } while (event.changes.any { it.pressed })
                                    // When the gesture ends, if not zoomed, reset everything.
                                    if (scale < 1.3f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    }
                                //}
                            }
                        }
                ) {
                    AsyncImage(
                        model = displayImages[page],
                        contentDescription = "$productName image ${page + 1}",
                        contentScale = ContentScale.Inside,
                        modifier = Modifier
                            //.padding(16.dp)
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                    )
                }
            }
            // Pager Indicator
            if (displayImages.size > 1) {
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(displayImages.size) { iteration ->
                        val color =
                            if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.White
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(6.dp)
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

//    Box(modifier = modifier.background(Color.White)) {
//        if (imageUrls.isNotEmpty()) {
//            AsyncImage(
//                model = ImageRequest.Builder(LocalContext.current)
//                    .data(currentVarImage ?: imageUrls.first()).build(),
//                //placeholder = painterResource(R.drawable.ic_product_placeholder),
//                //error = painterResource(R.drawable.ic_product_placeholder),
//                contentDescription = "$productName image",
//                contentScale = ContentScale.Inside,
//                modifier = Modifier.fillMaxSize()
//            )
//            // Consider adding a Pager for multiple images here
//        } else {
//            Image(
//                painter = painterResource(android.R.drawable.ic_menu_gallery),
//                contentDescription = "No image for $productName",
//                contentScale = ContentScale.Fit,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(32.dp)
//            )
//        }
//    }
}

@Composable
private fun ProductBasicInfo(
    product: ProductDetail,
    onClickBrand: (SimpleTerm) -> Unit = {}
) {

    product.brands.firstOrNull()?.let {
        Text(
            text = it.name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.8.sp,
            modifier = Modifier.clickable(onClick = { onClickBrand(it) })
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
    Text(
        text = product.name,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        //style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(6.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
    ) {
        product.concentration?.let { InfoChip(text = it) }
        product.volume?.let { InfoChip(text = it) }
        product.genderAffinity?.let { InfoChip(text = it) }
    }
}

@Composable
private fun ProductRatingSummary(averageRating: Float, reviewCount: Int, onClick: () -> Unit) {
    if (averageRating > 0 && reviewCount > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFFC107))
            Text(
                text = " ",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
            Text(
                text = " ($reviewCount reviews)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun CollapsibleSection(
    title: String,
    htmlContent: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    maxLinesCollapsed: Int = 4
) {
    Column {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        HtmlTextCompat(
            htmlString = htmlContent,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),

            modifier = Modifier
                .animateContentSize()
                .then(
                    if (!isExpanded) Modifier.heightIn(max = (maxLinesCollapsed * 22).dp) else Modifier
                )
            // Animates height changes
        )

        val approximateLineCount = 50
        val showToggleButton = (approximateLineCount > maxLinesCollapsed && !isExpanded) ||
                (htmlContent.length > 150 && !isExpanded) || // Existing heuristic
                (isExpanded && approximateLineCount > maxLinesCollapsed) // Show "Show Less" if expanded beyond threshold

        if (showToggleButton) {
            TextButton(
                onClick = onToggle,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 0.dp)
            ) {
                Text(if (isExpanded) stringResource(Res.string.show_less) else stringResource(Res.string.show_more))
            }
        }
    }
}


@Composable
private fun ProductAttributesInfo(attributes: List<ProductAttribute>, countryOfOrigin: String?) {
    if (attributes.isEmpty() && countryOfOrigin.isNullOrBlank()) return

    var expanded by remember { mutableStateOf(true) }

    ExpandableInfoCard(
        title = stringResource(Res.string.product_attributes_title),
        isExpanded = expanded,
        onToggle = { expanded = !expanded }
    ) {
        attributes.forEach { attr ->
            DetailRow(label = attr.name, value = attr.options.joinToString())
        }
        //countryOfOrigin?.let { DetailRow(label = "Brand Origin", value = it) }
    }
}


@Composable
private fun InfoPill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoChip(
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
) {
    Badge(containerColor = containerColor, contentColor = contentColorFor(containerColor)) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}


@Composable
private fun ExpandableInfoCard(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.6f)
        )
    }
}


@Composable
private fun ProductDetailBottomBar(
    price: Double,
    priceHtml: String?,
    onSales: Boolean = false,
    salesPrice: Double?,
    regularPrice: Double?,
    discountedPrice: (Double?) -> Double? = { null },
    cartQuantity: Int,
    maxQuantity: Int = 12,
    isVariationSelected: Boolean = true,
    stockStatus: String,
    isLowStock: Boolean = false,
    showInstallmentPrice: Boolean = false,
    onAddToCartClick: () -> Unit,
    onIncreaseItem: () -> Unit = {},
    onRemove: () -> Unit
) {

    val isAvailable = (stockStatus.equals("instock", ignoreCase = true) && isVariationSelected) ||
            (stockStatus.equals("onbackorder", ignoreCase = true) && isVariationSelected)
    val buttonText = when (stockStatus.lowercase()) {
        "outofstock" -> stringResource(Res.string.out_of_stock)
        "lowstock" -> stringResource(Res.string.add_to_cart_low)
        else -> stringResource(Res.string.add_to_cart)
    }

    Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp, tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                if (isAvailable) {
                    if (showInstallmentPrice) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.installment_pay),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(" x 4 ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            PriceView2(
                                price = price / 4,
                                onSale = onSales,
                                regularPrice = regularPrice?.let { it / 4 },
                                magnifier = 1.3
                            )
                        }

                        discountedPrice(price)?.let {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(Res.string.full_pay),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                PriceView2(
                                    price = discountedPrice(price) ?: price,
                                    onSale = onSales,
                                    regularPrice = discountedPrice(regularPrice)
                                        ?: regularPrice,
                                    magnifier = 1.0
                                )
                            }
                        }
                    } else {
                        PriceView2(
                            price = price,
                            onSale = onSales,
                            regularPrice = regularPrice,
                            magnifier = 1.2
                        )
                    }

                }

                if (!isVariationSelected) {
                    Text(
                        stringResource(Res.string.variation_not_selected_msg),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                } else if (!isAvailable) {
                    Text(
                        stringResource(Res.string.out_of_stock_msg),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                } else if (isLowStock) {
                    Text(
                        stringResource(Res.string.low_in_stock_msg),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            if (cartQuantity > 0) {
                QuantitySelector(
                    quantity = cartQuantity,
                    onAddMore = onIncreaseItem,
                    onRemove = onRemove,
                    maxQuantity = maxQuantity,
                    enabled = isAvailable // Disable selector if item goes out of stock
                )
            } else {
                Button(
                    onClick = onAddToCartClick,
                    enabled = isAvailable,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .defaultMinSize(minWidth = 140.dp),
                ) {
                    Icon(
                        Icons.Filled.ShoppingCart,
                        null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        buttonText,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

        }
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onAddMore: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    maxQuantity: Int = 12, // Sensible default maximum
    enabled: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        // Minus/Trash Button
        OutlinedIconButton(
            onClick = onRemove,
            modifier = Modifier.size(40.dp), // Consistent size
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(
                1.dp,
                if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.12f
                )
            ),
            enabled = enabled,
            colors = IconButtonDefaults.outlinedIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface, // Or transparent
                contentColor = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.2f
                )
            )
        ) {
            Icon(
                imageVector = if (quantity > 1) Icons.Filled.Remove else Icons.Outlined.Delete,
                contentDescription = if (quantity > 1) "Decrease quantity" else "Remove item"
            )
        }

        // Quantity Text
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 30.dp) // Ensure some minimum width for the text
        )

        // Plus Button
        OutlinedIconButton(
            onClick = {
                if (quantity < maxQuantity) {
                    onAddMore()
                }
            },
            modifier = Modifier.size(40.dp),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(
                1.dp,
                if (enabled && quantity < maxQuantity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.12f
                )
            ),
            enabled = enabled && quantity < maxQuantity,
            colors = IconButtonDefaults.outlinedIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = if (enabled && quantity < maxQuantity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.2f
                )
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Increase quantity"
            )
        }
    }
}


@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(errorMessage: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = "Error",
            tint = Color.Gray,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            errorMessage,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
    }
}

@Composable
fun NoReviewsPrompt(productName: String, onWriteReviewClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(Res.string.no_review_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                stringResource(Res.string.no_review_content, productName),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                shape = MaterialTheme.shapes.medium,
                onClick = onWriteReviewClick
            ) {
                Text(stringResource(Res.string.write_review_button))
            }
        }
    }
}

@Composable
private fun HtmlTextCompat(
    htmlString: String,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
) {
    Text(
        text = htmlString.stripHtmlTags(),
        style = style,
        modifier = modifier,
    )
}

private fun String.stripHtmlTags(): String = replace(Regex("<.*?>"), "")
