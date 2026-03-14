package com.solutionium.sharedui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.solutionium.shared.data.model.AttributeTerm
import com.solutionium.shared.data.model.DisplayableTerm
import com.solutionium.shared.data.model.PRODUCT_ARG_ATTRIBUTE
import com.solutionium.shared.data.model.PRODUCT_ARG_ATTRIBUTE_TERM
import com.solutionium.shared.data.model.PRODUCT_ARG_BRAND_ID
import com.solutionium.shared.data.model.PRODUCT_ARG_SEARCH
import com.solutionium.shared.data.model.PRODUCT_ARG_TITLE
import com.solutionium.shared.data.model.ProductCatType
import com.solutionium.shared.data.model.ProductThumbnail
import com.solutionium.shared.viewmodel.CategoryDisplayType
import com.solutionium.shared.viewmodel.CategoryScreenState
import com.solutionium.shared.viewmodel.CategoryViewModel
import com.solutionium.sharedui.common.component.CategoryScreenPlaceholder
import com.solutionium.sharedui.common.component.PerfumeAttributes2
import com.solutionium.sharedui.common.component.PriceView2
import com.solutionium.sharedui.common.component.SearchAppBar
import com.solutionium.sharedui.common.component.ShoeAttributes2
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.all_perfumes_title
import com.solutionium.sharedui.resources.in_stock
import com.solutionium.sharedui.resources.in_stock_count
import com.solutionium.sharedui.resources.no_results_found
import com.solutionium.sharedui.resources.out_of_stock
import com.solutionium.sharedui.resources.search
import com.solutionium.sharedui.resources.show_all_results
import com.solutionium.shared.data.model.SearchTabViewType
import org.jetbrains.compose.resources.stringResource


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryScreen(
    navigateToProductList: (Map<String, String>) -> Unit = {},
    onProductClick: (productId: Int) -> Unit = {},
    onNavigateBack: () -> Unit = {}, // Optional if it's a top-level screen
    viewModel: CategoryViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchTitle = stringResource(Res.string.search, uiState.searchQuery)

    val isRefreshing by viewModel.isRefreshing.collectAsState() // <-- Collect the refreshing state


    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(12.dp))
        SearchAppBar(
            query = uiState.searchQuery,
            onQueryChanged = viewModel::onSearchQueryChanged,
            onSearch = {
                navigateToProductList(
                    mapOf(
                        PRODUCT_ARG_SEARCH to uiState.searchQuery,
                        PRODUCT_ARG_TITLE to searchTitle,
                    )
                )
            },
            onClose = {
                if (uiState.searchQuery.isNotEmpty()) {
                    viewModel.clearSearch()
                } else {
                    onNavigateBack()
                }
            }
        )

        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                CategoryContent(
                    uiState = uiState,
                    navigateToProductList = navigateToProductList,
                    onProductClick = onProductClick,
                    onNavigateBack = onNavigateBack,
                    viewModel = viewModel
                )

                if (uiState.searchQuery.isNotBlank()) {
                    SearchPreview(
                        isLoading = uiState.isSearching,
                        results = uiState.searchResults,
                        onProductClick = onProductClick,
                        onShowAllClick = {
                            navigateToProductList(
                                mapOf(
                                    PRODUCT_ARG_SEARCH to uiState.searchQuery,
                                    PRODUCT_ARG_TITLE to searchTitle,
                                )
                            )
                        },
                        showStock = uiState.isSuperUser
                    )
                }

            }
        }
    }
}

@Composable
fun SearchPreview(
    isLoading: Boolean,
    results: List<ProductThumbnail>,
    onProductClick: (Int) -> Unit,
    onShowAllClick: () -> Unit,
    showStock: Boolean = false
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (results.isNotEmpty()) {
            items(results, key = { it.id }) { product ->
                // Use the new custom composable here
                SearchPreviewItem(
                    product = product,
                    onClick = { onProductClick(product.id) },
                    showStock = showStock
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                TextButton(onClick = onShowAllClick, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.show_all_results))
                }
            }
        } else {
            item {
                // You can make this more engaging
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp),
                    horizontalAlignment = CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "No results",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(Res.string.no_results_found),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchPreviewItem(
    product: ProductThumbnail,
    onClick: () -> Unit,
    showStock: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp), // Add padding for spacing
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product Image Thumbnail
        AsyncImage(
            model = product.imageUrl,
            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainer),
            error = ColorPainter(MaterialTheme.colorScheme.surfaceContainer),
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp) // A good size for a thumbnail in a list
                .clip(RoundedCornerShape(8.dp)) // Soften the corners
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Product Name and Price
        Column(
            modifier = Modifier.weight(1f) // Allow text to take remaining space
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (showStock) {
                val stockText = if (product.manageStock && product.stock > 0) {
                    stringResource(
                        Res.string.in_stock_count,
                        product.stock
                    )
                } else if (!product.manageStock && product.stockStatus == "instock") {
                    stringResource(Res.string.in_stock)
                } else {
                    stringResource(Res.string.out_of_stock)
                }
                Text(
                    text = stockText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (product.manageStock && product.stock > 0 || (!product.manageStock && product.stockStatus == "instock")) Color(
                        0xFF4CAF50
                    ) else Color(0xFFF44336), // Green or Red
                    //color = if (product.stock > 0) Color(0xFF4CAF50) else Color(0xFFF44336), // Green or Red
                    modifier = Modifier.heightIn(min = 18.dp) // Ensure consistent height
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(

                modifier = Modifier.fillMaxWidth()
            ) {

                PriceView2(product.price, product.onSale, product.regularPrice)
                Spacer(modifier = Modifier.weight(1f))
                when (product.type) {
                    ProductCatType.PERFUME -> PerfumeAttributes2(product, onlyVolume = true)
                    ProductCatType.SHOES -> ShoeAttributes2(product)
                    else -> {}
                }
            }

        }
    }
}


@Composable
fun CategoryContent(
    uiState: CategoryScreenState,
    navigateToProductList: (Map<String, String>) -> Unit = {},
    onProductClick: (productId: Int) -> Unit = {},
    onNavigateBack: () -> Unit = {}, // Optional if it's a top-level screen
    viewModel: CategoryViewModel
) {

    if (uiState.isLoading) {
        CategoryScreenPlaceholder()
    } else if (uiState.error != null) {
        Text(
            text = uiState.error.toString(),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                //.align(Alignment.Center)
                .padding(16.dp)
        )
    } else {

        when (uiState.categoryDisplayType) {
            CategoryDisplayType.MAIN -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    items(uiState.dynamicSections, key = { it.id }) { section ->
                        val onItemClick = { id: Int, title: String ->
                            val args = viewModel.toProductListArgsForItem(section, id, title)
                            if (args != null) {
                                navigateToProductList(args)
                            }
                        }
                        val onMoreClick = {
                            val moreLink = section.moreLink
                            if (moreLink != null) {
                                when (moreLink.type) {
                                    com.solutionium.shared.data.model.LinkType.ALL_BRANDS,
                                    com.solutionium.shared.data.model.LinkType.ATTRIBUTES -> viewModel.showAllItemsFromMore(section)
                                    else -> {
                                        val args = viewModel.toProductListArgsForMore(section)
                                        if (args != null) {
                                            navigateToProductList(args)
                                        }
                                    }
                                }
                            }
                        }

                        when (section.viewType) {
                            SearchTabViewType.SPOTLIGHT -> {
                                PerfumeSpotlightSection(
                                    spotlightTerms = section.items.filterIsInstance<AttributeTerm>(),
                                    title = section.title,
                                    imageFinder = { uiState.images[it] },
                                    onCategoryClick = onItemClick,
                                    onShopAllPerfumesClick = onMoreClick,
                                )
                            }

                            SearchTabViewType.CIRCLE_ROW -> {
                                SmallItemsSection(
                                    title = section.title,
                                    items = section.items,
                                    imageFinder = { uiState.images[it] },
                                    onClick = onItemClick,
                                    onViewAllClick = onMoreClick,
                                    viewAllText = section.moreTitle ?: section.moreLink?.title ?: "All",
                                )
                            }

                            SearchTabViewType.GRID -> {
                                ItemGridSection(
                                    title = section.title,
                                    viewAllText = section.moreTitle ?: section.moreLink?.title ?: "All",
                                    items = section.items,
                                    imageFinder = { uiState.images[it] },
                                    onClick = onItemClick,
                                    onShopAllClick = onMoreClick,
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            CategoryDisplayType.ALL_ITEMS -> {
                val allItemsState = uiState.allItemsState
                if (allItemsState == null) {
                    viewModel.backToMainDisplay()
                } else {
                    AllItemsScreen(
                        title = allItemsState.title,
                        items = allItemsState.items,
                        imageFinder = { uiState.images[it] },
                        onItemClick = { id, title ->
                            when (allItemsState.kind) {
                                com.solutionium.shared.viewmodel.CategoryAllItemsKind.BRAND -> {
                                    navigateToProductList(
                                        mapOf(
                                            PRODUCT_ARG_BRAND_ID to id.toString(),
                                            PRODUCT_ARG_TITLE to title,
                                        ),
                                    )
                                }

                                com.solutionium.shared.viewmodel.CategoryAllItemsKind.ATTRIBUTE -> {
                                    val attributeFilterKey = allItemsState.attributeFilterKey
                                        ?: allItemsState.attributeSource
                                        ?: return@AllItemsScreen
                                    navigateToProductList(
                                        mapOf(
                                            PRODUCT_ARG_ATTRIBUTE to attributeFilterKey,
                                            PRODUCT_ARG_ATTRIBUTE_TERM to id.toString(),
                                            PRODUCT_ARG_TITLE to title,
                                        ),
                                    )
                                }
                            }
                        },
                        onBack = { viewModel.backToMainDisplay() },
                    )
                }
            }
        }

    }

}

@Composable
fun PerfumeSpotlightSection(
    spotlightTerms: List<AttributeTerm>,
    title: String,
    imageFinder: (Int) -> String? = { null },
    onCategoryClick: (categoryId: Int, title: String) -> Unit,
    onShopAllPerfumesClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = title,
            onViewAllClick = onShopAllPerfumesClick,
            viewAllText = stringResource(Res.string.all_perfumes_title)
        )

        // Spotlight categories (e.g., For Him, For Her) could be larger cards or a LazyRow
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(spotlightTerms, key = { it.id }) { term ->
                SpotlightCategoryCard(
                    term = term,
                    image = imageFinder(term.id),
                    onClick = { onCategoryClick(term.id, term.name) })
            }
        }
    }
}

@Composable
fun SpotlightCategoryCard(
    term: AttributeTerm,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: String? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(280.dp)
            .height(180.dp), // Larger cards for spotlight
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            AsyncImage(
                model = image,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainer),
                error = ColorPainter(MaterialTheme.colorScheme.surfaceContainer),
                contentDescription = term.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box( // Gradient overlay for text readability
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                            startY = 180f * 0.4f // Gradient starts from 40% height
                        )
                    )
            )
            Text(
                text = term.name,
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun ItemGridSection(
    title: String,
    viewAllText: String = "All Categories",
    items: List<DisplayableTerm>,
    imageFinder: (Int) -> String? = { null },
    onClick: (termId: Int, title: String) -> Unit,
    onShopAllClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = title, onViewAllClick = onShopAllClick, viewAllText = viewAllText)

        // Using a simple Row for a few items, or a FlowRow for more. Max 2 per row for visual appeal.
        // Or a LazyVerticalGrid if many categories. For this example, simple rows:
        items.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowCategories.forEach { term ->
                    MediumItemCard(
                        term = term,
                        image = imageFinder(term.id),
                        onClick = { onClick(term.id, term.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowCategories.size == 1) { // Fill space if only one item in last row
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun MediumItemCard(
    term: DisplayableTerm,
    modifier: Modifier = Modifier,
    image: String? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(2f), // Square-ish cards
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            AsyncImage(
                model = term.imageUrl ?: image,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainer),
                error = ColorPainter(MaterialTheme.colorScheme.surfaceContainer),
                contentDescription = term.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box( // Gradient overlay for text readability
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Gray.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.4f)
                            ),
                            radius = 200f
                            //startY = 180f * 0.4f // Gradient starts from 40% height
                        )
                    )
            )
            Text(
                text = term.name,
                fontSize = MaterialTheme.typography.labelLarge.fontSize,
                maxLines = 1,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
            )

            Text(
                text = "(${term.count})",
                fontSize = MaterialTheme.typography.labelMedium.fontSize,
                maxLines = 1,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun SmallItemsSection(
    title: String,
    items: List<DisplayableTerm>,
    imageFinder: (Int) -> String? = { null },
    onClick: (id: Int, title: String) -> Unit,
    onViewAllClick: () -> Unit,
    viewAllText: String = "View All"
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = title, onViewAllClick = onViewAllClick, viewAllText = viewAllText)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { item ->
                SmallItemCard(
                    item = item,
                    image = imageFinder(item.id),
                    onClick = { onClick(item.id, item.name) }
                )
            }
        }
    }
}

@Composable
fun SmallItemCard(
    item: DisplayableTerm,
    modifier: Modifier = Modifier,
    image: String? = null,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        // Using OutlinedCard for a slightly different feel for brands
        //onClick = onClick,
        modifier = modifier
            .width(100.dp)
            .clickable { onClick() }, // Fixed width for brand logos
        //shape = RoundedCornerShape(8.dp),
        //border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = item.imageUrl ?: image,
                placeholder = ColorPainter(MaterialTheme.colorScheme.secondaryContainer),
                error = ColorPainter(MaterialTheme.colorScheme.secondaryContainer),
                contentDescription = item.name,
                modifier = Modifier
                    .size(60.dp)
                    .align(CenterHorizontally),
                // Adjust size as needed
                //.clip(CircleShape), // Circular logos
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.name,
                fontSize = MaterialTheme.typography.labelLarge.fontSize,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, onViewAllClick: () -> Unit, viewAllText: String = "View All") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        TextButton(onClick = onViewAllClick) {
            Text(viewAllText, fontWeight = FontWeight.Medium)
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .padding(start = 4.dp)
            )
        }
    }
}
