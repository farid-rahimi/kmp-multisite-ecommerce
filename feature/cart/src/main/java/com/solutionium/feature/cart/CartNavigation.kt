package com.solutionium.feature.cart

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.solutionium.sharedui.common.DestinationRoute
import com.solutionium.sharedui.cart.CartScreen
import com.solutionium.shared.viewmodel.CartViewModel
import org.koin.compose.koinInject


val GRAPH_CART_ROUTE = DestinationRoute("cart_graph_route")
private const val ROUTE_CART_SCREEN = "cart"

fun NavGraphBuilder.cartScreen(
    onCheckoutClick: (rootRoute: DestinationRoute) -> Unit,
    onProductClick: (rootRoute: DestinationRoute, id: Int) -> Unit,
    onNavigateAccount: () -> Unit,
    nestedGraphs: NavGraphBuilder.(DestinationRoute) -> Unit,
) {

    navigation(
        route = GRAPH_CART_ROUTE.route,
        startDestination = "${GRAPH_CART_ROUTE.route}/$ROUTE_CART_SCREEN",
    ) {
        composable(
            route = "${GRAPH_CART_ROUTE.route}/$ROUTE_CART_SCREEN",
        ) {
            val cartViewModel = koinInject<CartViewModel>()
            CartScreen(
                onCheckoutClick = { onCheckoutClick(GRAPH_CART_ROUTE)},
                onProductClick = { onProductClick(GRAPH_CART_ROUTE, it) },
                onNavigateToAccount = onNavigateAccount,
                viewModel = cartViewModel,
            )
        }
        nestedGraphs(GRAPH_CART_ROUTE)
    }
}
