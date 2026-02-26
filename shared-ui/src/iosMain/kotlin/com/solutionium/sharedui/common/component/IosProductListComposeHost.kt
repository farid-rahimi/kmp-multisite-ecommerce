package com.solutionium.sharedui.common.component

import androidx.compose.ui.window.ComposeUIViewController
import com.solutionium.shared.data.api.woo.getApiModule
import com.solutionium.shared.data.local.iosLocalModule
import com.solutionium.shared.viewmodel.HomeViewModel
import com.solutionium.shared.viewmodel.ProductDetailViewModel
import com.solutionium.shared.viewmodel.ReviewViewModel
import com.solutionium.shared.viewmodel.CategoryViewModel
import com.solutionium.shared.viewmodel.getCategoryModules
import com.solutionium.shared.viewmodel.getHomeModules
import com.solutionium.shared.viewmodel.iosAppModule
import com.solutionium.shared.viewmodel.getProductDetailModules
import com.solutionium.shared.viewmodel.getProductListModules
import com.solutionium.shared.viewmodel.getReviewModules
import com.solutionium.sharedui.designsystem.theme.WooTheme
import com.solutionium.sharedui.category.CategoryScreen
import com.solutionium.sharedui.home.HomeScreen
import com.solutionium.sharedui.products.ProductDetailScreen
import com.solutionium.sharedui.products.ProductListScreen
import com.solutionium.sharedui.review.ReviewListScreen
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import platform.UIKit.UIViewController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private fun ensureKoinStarted() {
    runCatching {
        startKoin {
            modules(
                (
                    getHomeModules() +
                    getCategoryModules() +
                    getProductListModules() +
                        getProductDetailModules() +
                        getReviewModules() +
                        getApiModule() +
                        setOf(iosLocalModule, iosAppModule)
                    ).toList(),
            )
        }
    }
}

private sealed interface IosShopRoute {
    data object Category : IosShopRoute
    data object Home : IosShopRoute
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
        ensureKoinStarted()
    }

    private val controller = ComposeUIViewController(
        configure = {
            enforceStrictPlistSanityCheck = false
        }
    ) {
        WooTheme {
            var route by remember { mutableStateOf<IosShopRoute>(IosShopRoute.Category) }

            when (val activeRoute = route) {
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

    fun viewController(): UIViewController = controller
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
