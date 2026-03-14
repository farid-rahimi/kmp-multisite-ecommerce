package com.solutionium.sharedui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.zIndex
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import com.solutionium.shared.viewmodel.AccountViewModel
import com.solutionium.shared.viewmodel.AddressViewModel
import com.solutionium.shared.viewmodel.CartViewModel
import com.solutionium.shared.viewmodel.CategoryViewModel
import com.solutionium.shared.viewmodel.CheckoutViewModel
import com.solutionium.shared.viewmodel.HomeNavigationEvent
import com.solutionium.shared.viewmodel.HomeViewModel
import com.solutionium.shared.viewmodel.OrderListViewModel
import com.solutionium.shared.viewmodel.OrderDetailsViewModel
import com.solutionium.shared.viewmodel.ProductDetailViewModel
import com.solutionium.shared.viewmodel.ReviewViewModel
import com.solutionium.sharedui.account.AccountAuthBottomSheet
import com.solutionium.sharedui.account.AccountScreen
import com.solutionium.sharedui.address.AddEditAddressScreen
import com.solutionium.sharedui.address.AddressListScreen
import com.solutionium.sharedui.cart.CartScreen
import com.solutionium.sharedui.category.CategoryScreen
import com.solutionium.sharedui.common.component.PlatformBottomNavigationBar
import com.solutionium.sharedui.common.component.platformAccountTabIcon
import com.solutionium.sharedui.common.component.platformBottomNavHeight
import com.solutionium.sharedui.common.component.platformCartTabIcon
import com.solutionium.sharedui.common.component.platformCategoryTabIcon
import com.solutionium.sharedui.common.component.platformHomeTabIcon
import com.solutionium.sharedui.common.component.platformShowTabLabelsAlways
import com.solutionium.sharedui.common.component.platformUsesCupertinoChrome
import com.solutionium.sharedui.checkout.CheckoutScreen
import com.solutionium.sharedui.home.HomeScreen
import com.solutionium.sharedui.home.PlatformStoryViewer
import com.solutionium.sharedui.orders.OrderDetailsScreen
import com.solutionium.sharedui.orders.OrderListScreen
import com.solutionium.sharedui.products.ProductDetailScreen
import com.solutionium.sharedui.products.ProductListScreen
import com.solutionium.sharedui.resources.Res
import com.solutionium.sharedui.resources.tab_account
import com.solutionium.sharedui.resources.tab_cart
import com.solutionium.sharedui.resources.tab_category
import com.solutionium.sharedui.resources.tab_home
import com.solutionium.sharedui.review.ReviewListScreen
import com.solutionium.shared.data.model.PRODUCT_ARG_IDS
import com.solutionium.shared.data.model.PRODUCT_ARG_TITLE
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private enum class MainTab(
    val title: StringResource,
) {
    Home(Res.string.tab_home),
    Category(Res.string.tab_category),
    Cart(Res.string.tab_cart),
    Account(Res.string.tab_account),
}

private sealed interface TabRoute {
    data class ProductList(val args: Map<String, String>) : TabRoute
    data class ProductDetail(val productId: Int, val fromListArgs: Map<String, String>?) : TabRoute
    data class Review(
        val productId: Int,
        val categoryIds: List<Int>,
        val fromListArgs: Map<String, String>?,
    ) : TabRoute

    data object OrderList : TabRoute
    data class OrderDetails(val orderId: Int) : TabRoute
    data object AddressList : TabRoute
    data class AddressEdit(val addressIdOrNew: Int) : TabRoute
    data class Checkout(
        val paymentReturnStatus: String? = null,
        val paymentReturnOrderId: Int? = null,
    ) : TabRoute
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SharedShopRoot(
    paymentReturnStatus: String? = null,
    paymentReturnOrderId: Int? = null,
    onPaymentReturnConsumed: () -> Unit = {},
    initialTabIndex: Int = 0,
    showBottomBar: Boolean = true,
    lockTabToInitial: Boolean = false,
) {
    val initialTab = remember(initialTabIndex) { tabFromIndex(initialTabIndex) }
    var activeTab by remember(initialTab) { mutableStateOf(initialTab) }
    val homeStack = remember { mutableStateListOf<TabRoute>() }
    val categoryStack = remember { mutableStateListOf<TabRoute>() }
    val cartStack = remember { mutableStateListOf<TabRoute>() }
    val accountStack = remember { mutableStateListOf<TabRoute>() }
    var showStoryViewer by remember { mutableStateOf(false) }
    var storyStartIndex by remember { mutableStateOf(0) }
    var showAuthSheet by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    val homeViewModel = koinInject<HomeViewModel>()
    val homeState by homeViewModel.state.collectAsState()
    val categoryViewModel = koinInject<CategoryViewModel>()
    val cartViewModel = koinInject<CartViewModel>()
    val accountViewModel = koinInject<AccountViewModel>()

    DisposableEffect(homeViewModel) {
        onDispose { homeViewModel.clear() }
    }
    DisposableEffect(categoryViewModel) {
        onDispose { categoryViewModel.clear() }
    }
    DisposableEffect(cartViewModel) {
        onDispose { cartViewModel.clear() }
    }
    DisposableEffect(accountViewModel) {
        onDispose { accountViewModel.clear() }
    }

    fun stackFor(tab: MainTab) = when (tab) {
        MainTab.Home -> homeStack
        MainTab.Category -> categoryStack
        MainTab.Cart -> cartStack
        MainTab.Account -> accountStack
    }

    fun pushToTab(tab: MainTab, route: TabRoute) {
        stackFor(tab).add(route)
    }

    fun popFromTab(tab: MainTab) {
        val stack = stackFor(tab)
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
    }

    fun switchTab(target: MainTab) {
        if (!lockTabToInitial || target == initialTab) {
            activeTab = target
        }
    }

    val topRoute = stackFor(activeTab).lastOrNull()
    val isFirstLoading = activeTab == MainTab.Home &&
        topRoute == null &&
        homeState.storiesLoading &&
        homeState.newArrivalsLoading &&
        homeState.appOffersLoading &&
        homeState.featuredLoading &&
        homeState.onSalesLoading
    val shouldShowBottomBar = showBottomBar && !isFirstLoading
    val useTransparentRoot = true//!showBottomBar && platformUsesCupertinoChrome()
    val activeStack = stackFor(activeTab)
    val shouldHandleBack = showStoryViewer ||
        activeStack.isNotEmpty() ||
        (!lockTabToInitial && activeTab != MainTab.Home)
    val isIos = remember { platformUsesCupertinoChrome() }


    LaunchedEffect(paymentReturnStatus, paymentReturnOrderId) {
        if (paymentReturnStatus.isNullOrBlank()) return@LaunchedEffect

        switchTab(MainTab.Cart)
        val route = TabRoute.Checkout(
            paymentReturnStatus = paymentReturnStatus,
            paymentReturnOrderId = paymentReturnOrderId,
        )
        if (cartStack.lastOrNull() is TabRoute.Checkout) {
            cartStack[cartStack.lastIndex] = route
        } else {
            cartStack.add(route)
        }
        onPaymentReturnConsumed()
    }

    BackHandler(enabled = shouldHandleBack) {
        when {
            showStoryViewer -> {
                showStoryViewer = false
                homeViewModel.persistViewedStories()
            }

            activeStack.isNotEmpty() -> {
                popFromTab(activeTab)
            }

            activeTab != MainTab.Home -> {
                switchTab(MainTab.Home)
            }
        }
    }

    LaunchedEffect(homeViewModel, uriHandler) {
        homeViewModel.navigationEvent.collect { event ->
            when (event) {
                is HomeNavigationEvent.ToProduct -> {
                    pushToTab(
                        MainTab.Home,
                        TabRoute.ProductDetail(
                            productId = event.productId,
                            fromListArgs = null,
                        ),
                    )
                }

                is HomeNavigationEvent.ToProductList -> {
                    pushToTab(MainTab.Home, TabRoute.ProductList(event.params))
                }

                is HomeNavigationEvent.ToExternalLink -> {
                    uriHandler.openUri(event.url)
                }
            }
        }
    }

    Scaffold(
        containerColor = if (useTransparentRoot) Color.Transparent else MaterialTheme.colorScheme.background,
        bottomBar = {
            if (shouldShowBottomBar) {
                if (platformUsesCupertinoChrome()) {
                    PlatformBottomNavigationBar(
                        modifier = Modifier.heightIn(min = platformBottomNavHeight()),
                    ) {
                        CupertinoTabItem(
                            selected = activeTab == MainTab.Home,
                            title = stringResource(MainTab.Home.title),
                            icon = platformHomeTabIcon(activeTab == MainTab.Home),
                            onClick = { switchTab(MainTab.Home) },
                        )
                        CupertinoTabItem(
                            selected = activeTab == MainTab.Category,
                            title = stringResource(MainTab.Category.title),
                            icon = platformCategoryTabIcon(activeTab == MainTab.Category),
                            onClick = { switchTab(MainTab.Category) },
                        )
                        CupertinoTabItem(
                            selected = activeTab == MainTab.Cart,
                            title = stringResource(MainTab.Cart.title),
                            icon = platformCartTabIcon(activeTab == MainTab.Cart),
                            onClick = { switchTab(MainTab.Cart) },
                        )
                        CupertinoTabItem(
                            selected = activeTab == MainTab.Account,
                            title = stringResource(MainTab.Account.title),
                            icon = platformAccountTabIcon(activeTab == MainTab.Account),
                            onClick = { switchTab(MainTab.Account) },
                        )
                    }
                } else {
                    PlatformBottomNavigationBar(
                        //modifier = Modifier.height(platformBottomNavHeight()),
                    ) {
                        NavigationBarItem(
                            selected = activeTab == MainTab.Home,
                            onClick = { switchTab(MainTab.Home) },
                            icon = { Icon(platformHomeTabIcon(activeTab == MainTab.Home), contentDescription = null) },
                            label = { Text(stringResource(MainTab.Home.title)) },
                            alwaysShowLabel = platformShowTabLabelsAlways(),
                        )
                        NavigationBarItem(
                            selected = activeTab == MainTab.Category,
                            onClick = { switchTab(MainTab.Category) },
                            icon = { Icon(platformCategoryTabIcon(activeTab == MainTab.Category), contentDescription = null) },
                            label = { Text(stringResource(MainTab.Category.title)) },
                            alwaysShowLabel = platformShowTabLabelsAlways(),
                        )
                        NavigationBarItem(
                            selected = activeTab == MainTab.Cart,
                            onClick = { switchTab(MainTab.Cart) },
                            icon = { Icon(platformCartTabIcon(activeTab == MainTab.Cart), contentDescription = null) },
                            label = { Text(stringResource(MainTab.Cart.title)) },
                            alwaysShowLabel = platformShowTabLabelsAlways(),
                        )
                        NavigationBarItem(
                            selected = activeTab == MainTab.Account,
                            onClick = { switchTab(MainTab.Account) },
                            icon = { Icon(platformAccountTabIcon(activeTab == MainTab.Account), contentDescription = null) },
                            label = { Text(stringResource(MainTab.Account.title)) },
                            alwaysShowLabel = platformShowTabLabelsAlways(),
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                //.padding(paddingValues)
                .padding(bottom = if (isIos) 0.dp else paddingValues.calculateBottomPadding())
            ,
            color = if (useTransparentRoot) Color.Transparent else MaterialTheme.colorScheme.background,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                MainTab.entries.forEach { tab ->
                    val route = stackFor(tab).lastOrNull()
                    val isTabActive = tab == activeTab
                    val layerModifier = Modifier
                        .fillMaxSize()
                        .zIndex(if (isTabActive) 1f else 0f)
                        .alpha(if (isTabActive) 1f else 0f)

                    Box(modifier = layerModifier) {
                        fun push(routeToPush: TabRoute) = pushToTab(tab, routeToPush)
                        fun pop() = popFromTab(tab)

                        when (route) {
                            null -> {
                                when (tab) {
                                    MainTab.Home -> {
                                        HomeScreen(
                                            onProductClick = { productId ->
                                                push(
                                                    TabRoute.ProductDetail(
                                                        productId = productId,
                                                        fromListArgs = null,
                                                    ),
                                                )
                                            },
                                            navigateToProductList = { args -> push(TabRoute.ProductList(args)) },
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
                                            navigateToProductList = { args -> push(TabRoute.ProductList(args)) },
                                            onProductClick = { productId ->
                                                push(
                                                    TabRoute.ProductDetail(
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
                                        CartScreen(
                                            viewModel = cartViewModel,
                                            onCheckoutClick = { push(TabRoute.Checkout()) },
                                            onProductClick = { productId ->
                                                push(
                                                    TabRoute.ProductDetail(
                                                        productId = productId,
                                                        fromListArgs = null,
                                                    ),
                                                )
                                            },
                                            onNavigateToAccount = { switchTab(MainTab.Account) },
                                        )
                                    }

                                    MainTab.Account -> {
                                        AccountScreen(
                                            onAddressClick = { push(TabRoute.AddressList) },
                                            onFavoriteClick = { title, ids ->
                                                push(
                                                    TabRoute.ProductList(
                                                        mapOf(
                                                            PRODUCT_ARG_TITLE to title,
                                                            PRODUCT_ARG_IDS to ids,
                                                        ),
                                                    ),
                                                )
                                            },
                                            onOrdersClick = { push(TabRoute.OrderList) },
                                            onOrderClick = { orderId -> push(TabRoute.OrderDetails(orderId)) },
                                            viewModel = accountViewModel,
                                            onBack = {},
                                        )
                                    }
                                }
                            }

                            is TabRoute.ProductList -> {
                                ProductListScreen(
                                    onProductClick = { productId ->
                                        push(
                                            TabRoute.ProductDetail(
                                                productId = productId,
                                                fromListArgs = route.args,
                                            ),
                                        )
                                    },
                                    onBack = { pop() },
                                    args = route.args,
                                )
                            }

                            is TabRoute.ProductDetail -> {
                                val viewModel = koinInject<ProductDetailViewModel>(
                                    parameters = { parametersOf(mapOf("productId" to route.productId.toString())) },
                                )
                                DisposableEffect(viewModel) {
                                    onDispose { viewModel.clear() }
                                }
                                ProductDetailScreen(
                                    viewModel = viewModel,
                                    onBackClick = { pop() },
                                    navigateToProductList = { args -> push(TabRoute.ProductList(args)) },
                                    onAllReviewClicked = { productId, categoryIds ->
                                        push(
                                            TabRoute.Review(
                                                productId = productId,
                                                categoryIds = categoryIds,
                                                fromListArgs = route.fromListArgs,
                                            ),
                                        )
                                    },
                                )
                            }

                            is TabRoute.Review -> {
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
                                    onLoginToReviewClick = { showAuthSheet = true },
                                    authSheetVisible = showAuthSheet,
                                )
                            }

                            TabRoute.OrderList -> {
                                val viewModel = koinInject<OrderListViewModel>()
                                DisposableEffect(viewModel) {
                                    onDispose { viewModel.clear() }
                                }
                                OrderListScreen(
                                    onOrderClick = { orderId -> push(TabRoute.OrderDetails(orderId)) },
                                    onBack = { pop() },
                                    viewModel = viewModel,
                                )
                            }

                            is TabRoute.OrderDetails -> {
                                val viewModel = koinInject<OrderDetailsViewModel>(
                                    parameters = {
                                        parametersOf(
                                            mapOf("order_id" to route.orderId.toString()),
                                        )
                                    },
                                )
                                DisposableEffect(viewModel) {
                                    onDispose { viewModel.clear() }
                                }
                                OrderDetailsScreen(
                                    onBack = { pop() },
                                    viewModel = viewModel,
                                )
                            }

                            TabRoute.AddressList -> {
                                val viewModel = koinInject<AddressViewModel>(
                                    parameters = { parametersOf(emptyMap<String, String>()) },
                                )
                                DisposableEffect(viewModel) {
                                    onDispose { viewModel.clear() }
                                }
                                AddressListScreen(
                                    onNavigateToEditAddress = { addressId ->
                                        push(TabRoute.AddressEdit(addressId ?: -1))
                                    },
                                    onBackNavigation = { pop() },
                                    viewModel = viewModel,
                                )
                            }

                            is TabRoute.AddressEdit -> {
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

                            is TabRoute.Checkout -> {
                                val viewModel = koinInject<CheckoutViewModel>()
                                DisposableEffect(viewModel) {
                                    onDispose { viewModel.clear() }
                                }
                                CheckoutScreen(
                                    onBack = {
                                        viewModel.resetOrderStatus()
                                        pop()
                                    },
                                    onAddEditAddressClick = { addressId ->
                                        push(TabRoute.AddressEdit(addressId ?: -1))
                                    },
                                    onContinueShopping = {
                                        viewModel.resetOrderStatus()
                                        cartStack.clear()
                                        switchTab(MainTab.Home)
                                    },
                                    paymentReturnStatus = route.paymentReturnStatus,
                                    paymentReturnOrderId = route.paymentReturnOrderId,
                                    viewModel = viewModel,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStoryViewer && homeState.storyItems.isNotEmpty() && homeStack.lastOrNull() == null && activeTab == MainTab.Home) {
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

    if (showAuthSheet) {
        AccountAuthBottomSheet(
            viewModel = accountViewModel,
            onDismiss = { showAuthSheet = false },
        )
    }
}

private fun tabFromIndex(index: Int): MainTab {
    return when (index) {
        1 -> MainTab.Category
        2 -> MainTab.Cart
        3 -> MainTab.Account
        else -> MainTab.Home
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.CupertinoTabItem(
    selected: Boolean,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val activeTint = MaterialTheme.colorScheme.primary
    val inactiveTint = MaterialTheme.colorScheme.onSurfaceVariant
    val tint = if (selected) activeTint else inactiveTint

    Box(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = title,
                color = tint,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
            )
        }
    }
}
