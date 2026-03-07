package com.solutionium.feature.category

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.solutionium.sharedui.common.DestinationRoute
import com.solutionium.sharedui.category.CategoryScreen
import com.solutionium.shared.viewmodel.CategoryViewModel
import org.koin.compose.koinInject

val GRAPH_CATEGORY_ROUTE = DestinationRoute("category_graph_route")
private const val ROUTE_CATEGORY_SCREEN = "category"

fun NavGraphBuilder.categoryScreen(
    navigateToProductList: (route: DestinationRoute, params: Map<String, String>) -> Unit = { _, _ -> },
    onProductClick: (rootRoute: DestinationRoute, id: Int) -> Unit,
    onNavigateBack: () -> Unit,
    nestedGraphs: NavGraphBuilder.(DestinationRoute) -> Unit,
) {

    navigation(
        route = GRAPH_CATEGORY_ROUTE.route,
        startDestination = "${GRAPH_CATEGORY_ROUTE.route}/$ROUTE_CATEGORY_SCREEN",
    ) {
        composable(
            route = "${GRAPH_CATEGORY_ROUTE.route}/$ROUTE_CATEGORY_SCREEN",
        ) {
            val categoryViewModel = koinInject<CategoryViewModel>()
            CategoryScreen(
                navigateToProductList = { navigateToProductList(GRAPH_CATEGORY_ROUTE, it) },
                onNavigateBack = {},
                onProductClick = { onProductClick(GRAPH_CATEGORY_ROUTE, it) },
                viewModel = categoryViewModel,
            )
        }
        nestedGraphs(GRAPH_CATEGORY_ROUTE)
    }

}
