package com.solutionium.sharedui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.solutionium.sharedui.account.AccountScreen
import com.solutionium.sharedui.address.AddEditAddressScreen
import com.solutionium.sharedui.address.AddressListScreen
import com.solutionium.sharedui.cart.CartScreen
import com.solutionium.sharedui.category.CategoryScreen
import com.solutionium.sharedui.home.HomeScreen
import com.solutionium.sharedui.home.PlatformStoryViewer
import com.solutionium.sharedui.orders.OrderListScreen
import com.solutionium.sharedui.products.ProductDetailScreen
import com.solutionium.sharedui.products.ProductListScreen
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.tab_account
import com.solutionium.sharedui.resources.tab_cart
import com.solutionium.sharedui.resources.tab_category
import com.solutionium.sharedui.resources.tab_home
import com.solutionium.sharedui.review.ReviewListScreen
import com.solutionium.shared.viewmodel.AccountViewModel
import com.solutionium.shared.viewmodel.AddressViewModel
import com.solutionium.shared.viewmodel.CartViewModel
import com.solutionium.shared.viewmodel.CategoryViewModel
import com.solutionium.shared.viewmodel.HomeViewModel
import com.solutionium.shared.viewmodel.HomeNavigationEvent
import com.solutionium.shared.viewmodel.OrderListViewModel
import com.solutionium.shared.viewmodel.ProductDetailViewModel
import com.solutionium.shared.viewmodel.ReviewViewModel
import kotlinx.coroutines.flow.collect
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

private enum class MainTab(
    val title: StringResource,
) {
    Home(Res.string.tab_home),
    Category(Res.string.tab_category),
    Cart(Res.string.tab_cart),
    Account(Res.string.tab_account),
}

private sealed interface OverlayRoute {
    data class ProductList(val args: Map<String, String>) : OverlayRoute
    data class ProductDetail(val productId: Int, val fromListArgs: Map<String, String>?) : OverlayRoute
    data class Review(
        val productId: Int,
        val categoryIds: List<Int>,
        val fromListArgs: Map<String, String>?,
    ) : OverlayRoute

    data object OrderList : OverlayRoute
    data object AddressList : OverlayRoute
    data class AddressEdit(val addressIdOrNew: Int) : OverlayRoute
}

@Composable
fun SharedShopRoot() {
    var activeTab by remember { mutableStateOf(MainTab.Home) }
    val overlays = remember { mutableStateListOf<OverlayRoute>() }
    val topOverlay = overlays.lastOrNull()
    var showStoryViewer by remember { mutableStateOf(false) }
    var storyStartIndex by remember { mutableStateOf(0) }
    val uriHandler = LocalUriHandler.current

    val homeViewModel = koinInject<HomeViewModel>()
    val homeState by homeViewModel.state.collectAsState()
    val categoryViewModel = koinInject<CategoryViewModel>()

    DisposableEffect(homeViewModel) {
        onDispose { homeViewModel.clear() }
    }
    DisposableEffect(categoryViewModel) {
        onDispose { categoryViewModel.clear() }
    }

    fun push(route: OverlayRoute) {
        overlays.add(route)
    }

    fun pop() {
        if (overlays.isNotEmpty()) overlays.removeAt(overlays.lastIndex)
    }

    LaunchedEffect(homeViewModel, uriHandler) {
        homeViewModel.navigationEvent.collect { event ->
            when (event) {
                is HomeNavigationEvent.ToProduct -> {
                    push(
                        OverlayRoute.ProductDetail(
                            productId = event.productId,
                            fromListArgs = null,
                        ),
                    )
                }

                is HomeNavigationEvent.ToProductList -> {
                    push(OverlayRoute.ProductList(event.params))
                }

                is HomeNavigationEvent.ToExternalLink -> {
                    uriHandler.openUri(event.url)
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (topOverlay == null) {
                NavigationBar {
                    NavigationBarItem(
                        selected = activeTab == MainTab.Home,
                        onClick = { activeTab = MainTab.Home },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text(stringResource(MainTab.Home.title)) },
                    )
                    NavigationBarItem(
                        selected = activeTab == MainTab.Category,
                        onClick = { activeTab = MainTab.Category },
                        icon = { Icon(Icons.Filled.Category, contentDescription = null) },
                        label = { Text(stringResource(MainTab.Category.title)) },
                    )
                    NavigationBarItem(
                        selected = activeTab == MainTab.Cart,
                        onClick = { activeTab = MainTab.Cart },
                        icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
                        label = { Text(stringResource(MainTab.Cart.title)) },
                    )
                    NavigationBarItem(
                        selected = activeTab == MainTab.Account,
                        onClick = { activeTab = MainTab.Account },
                        icon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                        label = { Text(stringResource(MainTab.Account.title)) },
                    )
                }
            }
        },
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (val route = topOverlay) {
                null -> {
                    when (activeTab) {
                        MainTab.Home -> {
                            HomeScreen(
                                onProductClick = { productId ->
                                    push(
                                        OverlayRoute.ProductDetail(
                                            productId = productId,
                                            fromListArgs = null,
                                        ),
                                    )
                                },
                                navigateToProductList = { args -> push(OverlayRoute.ProductList(args)) },
                                onStoryClick = { clickedStory ->
                                    val index = homeState.storyItems.indexOf(clickedStory)
                                    storyStartIndex = if (index >= 0) index else 0
                                    showStoryViewer = true
                                },
                                viewModel = homeViewModel,
                            )
                        }

                        MainTab.Category -> {
                            CategoryScreen(
                                navigateToProductList = { args -> push(OverlayRoute.ProductList(args)) },
                                onProductClick = { productId ->
                                    push(
                                        OverlayRoute.ProductDetail(
                                            productId = productId,
                                            fromListArgs = null,
                                        ),
                                    )
                                },
                                onNavigateBack = {},
                                viewModel = categoryViewModel,
                            )
                        }

                        MainTab.Cart -> {
                            val viewModel = koinInject<CartViewModel>()
                            CartScreen(
                                viewModel = viewModel,
                                onCheckoutClick = {},
                                onProductClick = { productId ->
                                    push(
                                        OverlayRoute.ProductDetail(
                                            productId = productId,
                                            fromListArgs = null,
                                        ),
                                    )
                                },
                                onNavigateToAccount = { activeTab = MainTab.Account },
                            )
                        }

                        MainTab.Account -> {
                            val viewModel = koinInject<AccountViewModel>()
                            DisposableEffect(viewModel) {
                                onDispose { viewModel.clear() }
                            }
                            AccountScreen(
                                onAddressClick = { push(OverlayRoute.AddressList) },
                                onFavoriteClick = { title, ids ->
                                    push(OverlayRoute.ProductList(mapOf("title" to title, "ids" to ids)))
                                },
                                onOrdersClick = { push(OverlayRoute.OrderList) },
                                onOrderClick = { push(OverlayRoute.OrderList) },
                                viewModel = viewModel,
                                onBack = {},
                            )
                        }
                    }
                }

                is OverlayRoute.ProductList -> {
                    ProductListScreen(
                        onProductClick = { productId ->
                            push(
                                OverlayRoute.ProductDetail(
                                    productId = productId,
                                    fromListArgs = route.args,
                                ),
                            )
                        },
                        onBack = { pop() },
                        args = route.args,
                    )
                }

                is OverlayRoute.ProductDetail -> {
                    val viewModel = koinInject<ProductDetailViewModel>(
                        parameters = { parametersOf(mapOf("productId" to route.productId.toString())) },
                    )
                    DisposableEffect(viewModel) {
                        onDispose { viewModel.clear() }
                    }
                    ProductDetailScreen(
                        viewModel = viewModel,
                        onBackClick = { pop() },
                        navigateToProductList = { args -> push(OverlayRoute.ProductList(args)) },
                        onAllReviewClicked = { productId, categoryIds ->
                            push(
                                OverlayRoute.Review(
                                    productId = productId,
                                    categoryIds = categoryIds,
                                    fromListArgs = route.fromListArgs,
                                ),
                            )
                        },
                    )
                }

                is OverlayRoute.Review -> {
                    val viewModel = koinInject<ReviewViewModel>(
                        parameters = {
                            parametersOf(
                                mapOf(
                                    "productId" to route.productId.toString(),
                                    "categoryIds" to route.categoryIds.joinToString(","),
                                ),
                            )
                        },
                    )
                    DisposableEffect(viewModel) {
                        onDispose { viewModel.clear() }
                    }
                    ReviewListScreen(
                        viewModel = viewModel,
                        onBackClick = { pop() },
                    )
                }

                OverlayRoute.OrderList -> {
                    val viewModel = koinInject<OrderListViewModel>()
                    DisposableEffect(viewModel) {
                        onDispose { viewModel.clear() }
                    }
                    OrderListScreen(
                        onOrderClick = {},
                        onBack = { pop() },
                        viewModel = viewModel,
                    )
                }

                OverlayRoute.AddressList -> {
                    val viewModel = koinInject<AddressViewModel>(
                        parameters = { parametersOf(emptyMap<String, String>()) },
                    )
                    DisposableEffect(viewModel) {
                        onDispose { viewModel.clear() }
                    }
                    AddressListScreen(
                        onNavigateToEditAddress = { addressId ->
                            push(OverlayRoute.AddressEdit(addressId ?: -1))
                        },
                        onBackNavigation = { pop() },
                        viewModel = viewModel,
                    )
                }

                is OverlayRoute.AddressEdit -> {
                    val viewModel = koinInject<AddressViewModel>(
                        parameters = {
                            parametersOf(
                                mapOf("address_id_or_new" to route.addressIdOrNew.toString()),
                            )
                        },
                    )
                    DisposableEffect(viewModel) {
                        onDispose { viewModel.clear() }
                    }
                    AddEditAddressScreen(
                        onSaved = { pop() },
                        onBack = { pop() },
                        viewModel = viewModel,
                    )
                }
            }
        }
    }

    if (showStoryViewer && homeState.storyItems.isNotEmpty() && topOverlay == null && activeTab == MainTab.Home) {
        PlatformStoryViewer(
            stories = homeState.storyItems,
            startIndex = storyStartIndex,
            onClose = {
                showStoryViewer = false
                homeViewModel.persistViewedStories()
            },
            onLinkClick = { link ->
                showStoryViewer = false
                homeViewModel.persistViewedStories()
                homeViewModel.onLinkClick(link)
            },
            onStoryViewed = { storyId ->
                homeViewModel.setStoryAsViewedInSession(storyId)
            },
        )
    }
}
