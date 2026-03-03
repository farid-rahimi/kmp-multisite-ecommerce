package com.solutionium.feature.account

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.solutionium.sharedui.common.DestinationRoute
import com.solutionium.shared.data.model.PRODUCT_ARG_IDS
import com.solutionium.shared.data.model.PRODUCT_ARG_TITLE
import com.solutionium.shared.viewmodel.AccountViewModel
import org.koin.compose.koinInject


val GRAPH_ACCOUNT_ROUTE = DestinationRoute("account_graph_route")
private const val ROUTE_ACCOUNT_SCREEN = "account"

fun NavGraphBuilder.accountScreen(
    onAddressClick: (rootRoute: DestinationRoute) -> Unit,
    navigateToProductList: (route: DestinationRoute, params: Map<String, String>) -> Unit = { _, _ -> },
    onOrdersClick: (rootRoute: DestinationRoute) -> Unit,
    onOrderClick: (rootRoute: DestinationRoute, orderId: Int) -> Unit,
    nestedGraphs: NavGraphBuilder.(DestinationRoute) -> Unit,
    onBack: () -> Unit
) {

    navigation(
        route = GRAPH_ACCOUNT_ROUTE.route,
        startDestination = "${GRAPH_ACCOUNT_ROUTE.route}/$ROUTE_ACCOUNT_SCREEN",
    ) {
        composable(
            route = "${GRAPH_ACCOUNT_ROUTE.route}/$ROUTE_ACCOUNT_SCREEN",
        ) {
            AccountScreen(
                onAddressClick = { onAddressClick(GRAPH_ACCOUNT_ROUTE) },
                onFavoriteClick = { title, ids ->
                    navigateToProductList(
                        GRAPH_ACCOUNT_ROUTE,
                        mapOf(PRODUCT_ARG_TITLE to title, PRODUCT_ARG_IDS to ids)
                    )
                },
                onOrdersClick = { onOrdersClick(GRAPH_ACCOUNT_ROUTE) },
                onOrderClick = { onOrderClick(GRAPH_ACCOUNT_ROUTE, it) },
                viewModel = koinInject<AccountViewModel>(),
                onBack = onBack
            )
        }
        nestedGraphs(GRAPH_ACCOUNT_ROUTE)
    }
}

fun NavController.navigateToAccount() {
    navigate(GRAPH_ACCOUNT_ROUTE.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
