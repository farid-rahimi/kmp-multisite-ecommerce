package com.solutionium.feature.product.detail

import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.solutionium.sharedui.common.DestinationRoute
import com.solutionium.sharedui.products.ProductDetailScreen as SharedProductDetailScreen
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

const val GRAPH_PRODUCT_ROUTE = "product_graph_route"
fun NavGraphBuilder.productDetailScreen(
    rootRoute: DestinationRoute,
    onAllReviewClicked: (DestinationRoute, Int, List<Int>) -> Unit,
    navigateToProductList: (route: DestinationRoute, Map<String, String>) -> Unit,
    onBackClick: () -> Unit,
) {
    composable(
        route = "${rootRoute.route}/product?id={productId}&slug={productSlug}",
        arguments = listOf(
            navArgument("productId") {
                type = NavType.IntType
                defaultValue = -1
            },
            navArgument("productSlug") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        ),

    ) { navBackStackEntry ->
        val args = buildMap {
            put("productId", navBackStackEntry.arguments?.getInt("productId", -1).toString())
            navBackStackEntry.arguments?.getString("productSlug")?.let { put("productSlug", it) }
        }
        val viewModel = koinInject<com.solutionium.shared.viewmodel.ProductDetailViewModel>(
            parameters = { parametersOf(args) },
        )
        DisposableEffect(viewModel) {
            onDispose { viewModel.clear() }
        }

        SharedProductDetailScreen(
            viewModel = viewModel,
            onAllReviewClicked = { id, catIds -> onAllReviewClicked(rootRoute, id, catIds) },
            navigateToProductList = { params -> navigateToProductList(rootRoute, params) },
            onBackClick = onBackClick
        )
    }
}

fun NavController.navigateProductDetail(
    rootRoute: DestinationRoute,
    productId: Int
) {
    navigate("${rootRoute.route}/product?id=$productId")
    //navigate("${rootRoute.route}/product/$productId")
}

fun NavController.navigateProductDetail(
    rootRoute: DestinationRoute,
    productSlug: String
) {
    navigate("${rootRoute.route}/product?slug=$productSlug")
}
