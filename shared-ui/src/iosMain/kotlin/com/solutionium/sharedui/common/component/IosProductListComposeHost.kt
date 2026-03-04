package com.solutionium.sharedui.common.component

import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.solutionium.shared.viewmodel.HomeViewModel
import com.solutionium.shared.viewmodel.ProductDetailViewModel
import com.solutionium.shared.viewmodel.ReviewViewModel
import com.solutionium.shared.viewmodel.CategoryViewModel
import com.solutionium.shared.viewmodel.AccountViewModel
import com.solutionium.shared.viewmodel.AddressViewModel
import com.solutionium.shared.viewmodel.CartViewModel
import com.solutionium.shared.viewmodel.OrderListViewModel
import com.solutionium.sharedui.bootstrap.IosKoinBridge
import com.solutionium.sharedui.bootstrap.IosRuntimeConfig
import com.solutionium.sharedui.account.AccountScreen
import com.solutionium.sharedui.address.AddEditAddressScreen
import com.solutionium.sharedui.address.AddressListScreen
import com.solutionium.sharedui.cart.CartScreen
import com.solutionium.sharedui.designsystem.theme.WooTheme
import com.solutionium.sharedui.category.CategoryScreen
import com.solutionium.sharedui.home.HomeScreen
import com.solutionium.sharedui.orders.OrderListScreen
import com.solutionium.sharedui.products.ProductDetailScreen
import com.solutionium.sharedui.products.ProductListScreen
import com.solutionium.sharedui.review.ReviewListScreen
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import platform.UIKit.UIViewController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private sealed interface IosShopRoute {
    data object Account : IosShopRoute
    data object AddressList : IosShopRoute
    data class AddressEdit(val addressIdOrNew: Int) : IosShopRoute
    data object Cart : IosShopRoute
    data object Category : IosShopRoute
    data object Home : IosShopRoute
    data object OrderList : IosShopRoute
    data class ProductList(val args: Map<String, String>) : IosShopRoute
    data class Detail(val productId: Int, val fromListArgs: Map<String, String>?) : IosShopRoute
    data class Review(
        val productId: Int,
        val categoryIds: List<Int>,
        val fromListArgs: Map<String, String>?,
    ) : IosShopRoute
}

class IosProductListComposeHost {
    init {
        IosKoinBridge().initKoinDefault()
    }

    private val controller = ComposeUIViewController(
        configure = {
            enforceStrictPlistSanityCheck = false
        }
    ) {
        WooTheme(brand = IosRuntimeConfig.brand) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                var route by remember { mutableStateOf<IosShopRoute>(IosShopRoute.Account) }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (val activeRoute = route) {
                        IosShopRoute.Account -> {
                            IosAccountRoute(
                                onOpenHome = { route = IosShopRoute.Home },
                                onOpenAddress = { route = IosShopRoute.AddressList },
                                onOpenOrders = { route = IosShopRoute.OrderList },
                                onOpenProductList = { args -> route = IosShopRoute.ProductList(args) },
                            )
                        }

                        IosShopRoute.AddressList -> {
                            IosAddressListRoute(
                                onBack = { route = IosShopRoute.Account },
                                onEditAddress = { addressIdOrNew ->
                                    route = IosShopRoute.AddressEdit(addressIdOrNew)
                                },
                            )
                        }

                        is IosShopRoute.AddressEdit -> {
                            IosAddressEditRoute(
                                addressIdOrNew = activeRoute.addressIdOrNew,
                                onBack = { route = IosShopRoute.AddressList },
                            )
                        }

                        IosShopRoute.Cart -> {
                            IosCartRoute()
                        }

                        IosShopRoute.Category -> {
                            IosCategoryRoute(
                                onProductClick = { productId ->
                                    route = IosShopRoute.Detail(
                                        productId = productId,
                                        fromListArgs = null,
                                    )
                                },
                                onOpenProductList = { args ->
                                    route = IosShopRoute.ProductList(args)
                                },
                            )
                        }

                        IosShopRoute.Home -> {
                            IosHomeRoute(
                                onProductClick = { productId ->
                                    route = IosShopRoute.Detail(
                                        productId = productId,
                                        fromListArgs = null,
                                    )
                                },
                                onOpenProductList = { args ->
                                    route = IosShopRoute.ProductList(args)
                                },
                            )
                        }

                        IosShopRoute.OrderList -> {
                            IosOrderListRoute(
                                onBack = { route = IosShopRoute.Account },
                            )
                        }

                        is IosShopRoute.ProductList -> {
                            ProductListScreen(
                                onProductClick = {
                                    route = IosShopRoute.Detail(
                                        productId = it,
                                        fromListArgs = activeRoute.args,
                                    )
                                },
                                onBack = { route = IosShopRoute.Category },
                                args = activeRoute.args,
                            )
                        }

                        is IosShopRoute.Detail -> {
                            IosProductDetailRoute(
                                productId = activeRoute.productId,
                                onBackClick = {
                                    route = activeRoute.fromListArgs?.let { args ->
                                        IosShopRoute.ProductList(args)
                                    } ?: IosShopRoute.Category
                                },
                                onOpenProductList = { args ->
                                    route = IosShopRoute.ProductList(args)
                                },
                                onAllReviewClicked = { productId, categoryIds ->
                                    route = IosShopRoute.Review(
                                        productId = productId,
                                        categoryIds = categoryIds,
                                        fromListArgs = activeRoute.fromListArgs,
                                    )
                                },
                            )
                        }

                        is IosShopRoute.Review -> {
                            IosReviewRoute(
                                productId = activeRoute.productId,
                                categoryIds = activeRoute.categoryIds,
                                onBack = {
                                    route = IosShopRoute.Detail(
                                        productId = activeRoute.productId,
                                        fromListArgs = activeRoute.fromListArgs,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    fun viewController(): UIViewController = controller
}

@Composable
private fun IosAccountRoute(
    onOpenHome: () -> Unit,
    onOpenAddress: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenProductList: (Map<String, String>) -> Unit,
) {
    val viewModel = koinInject<AccountViewModel>()

    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }

    AccountScreen(
        onAddressClick = onOpenAddress,
        onFavoriteClick = { title, ids ->
            onOpenProductList(
                mapOf(
                    "title" to title,
                    "ids" to ids,
                ),
            )
        },
        onOrdersClick = onOpenOrders,
        onOrderClick = { onOpenOrders() },
        viewModel = viewModel,
        onBack = onOpenHome,
    )
}

@Composable
private fun IosAddressListRoute(
    onBack: () -> Unit,
    onEditAddress: (Int) -> Unit,
) {
    val viewModel = koinInject<AddressViewModel>(
        parameters = { parametersOf(emptyMap<String, String>()) },
    )

    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }

    AddressListScreen(
        onNavigateToEditAddress = { addressId -> onEditAddress(addressId ?: -1) },
        onBackNavigation = onBack,
        viewModel = viewModel,
    )
}

@Composable
private fun IosAddressEditRoute(
    addressIdOrNew: Int,
    onBack: () -> Unit,
) {
    val viewModel = koinInject<AddressViewModel>(
        parameters = {
            parametersOf(
                mapOf("address_id_or_new" to addressIdOrNew.toString()),
            )
        },
    )

    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }

    AddEditAddressScreen(
        onSaved = onBack,
        onBack = onBack,
        viewModel = viewModel,
    )
}

@Composable
private fun IosOrderListRoute(
    onBack: () -> Unit,
) {
    val viewModel = koinInject<OrderListViewModel>()

    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }

    OrderListScreen(
        onOrderClick = {},
        onBack = onBack,
        viewModel = viewModel,
    )
}

@Composable
private fun IosCategoryRoute(
    onProductClick: (Int) -> Unit,
    onOpenProductList: (Map<String, String>) -> Unit,
) {
    val viewModel = koinInject<CategoryViewModel>()

    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }

    CategoryScreen(
        navigateToProductList = onOpenProductList,
        onProductClick = onProductClick,
        onNavigateBack = {},
        viewModel = viewModel,
    )
}

@Composable
private fun IosHomeRoute(
    onProductClick: (Int) -> Unit,
    onOpenProductList: (Map<String, String>) -> Unit,
) {
    val viewModel = koinInject<HomeViewModel>()

    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }

    HomeScreen(
        onProductClick = onProductClick,
        navigateToProductList = onOpenProductList,
        onStoryClick = {},
        viewModel = viewModel,
    )
}

@Composable
private fun IosReviewRoute(
    productId: Int,
    categoryIds: List<Int>,
    onBack: () -> Unit,
) {
    val viewModel = koinInject<ReviewViewModel>(
        parameters = {
            parametersOf(
                mapOf(
                    "productId" to productId.toString(),
                    "categoryIds" to categoryIds.joinToString(","),
                ),
            )
        },
    )

    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }

    ReviewListScreen(
        viewModel = viewModel,
        onBackClick = onBack,
    )
}

@Composable
private fun IosProductDetailRoute(
    productId: Int,
    onBackClick: () -> Unit,
    onOpenProductList: (Map<String, String>) -> Unit,
    onAllReviewClicked: (Int, List<Int>) -> Unit,
) {
    val viewModel = koinInject<ProductDetailViewModel>(
        parameters = { parametersOf(mapOf("productId" to productId.toString())) },
    )

    DisposableEffect(viewModel) {
        onDispose { viewModel.clear() }
    }

    ProductDetailScreen(
        viewModel = viewModel,
        onAllReviewClicked = onAllReviewClicked,
        navigateToProductList = onOpenProductList,
        onBackClick = onBackClick,
    )
}

@Composable
private fun IosCartRoute() {
    val viewModel = koinInject<CartViewModel>()

    CartScreen(
        viewModel = viewModel,
        onCheckoutClick = {},
        onProductClick = {},
        onNavigateToAccount = {},
    )
}
