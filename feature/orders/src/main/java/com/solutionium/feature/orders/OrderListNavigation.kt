package com.solutionium.feature.orders

import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.solutionium.sharedui.common.DestinationRoute
import com.solutionium.sharedui.orders.OrderListScreen
import com.solutionium.shared.viewmodel.OrderListViewModel
import org.koin.compose.koinInject


private const val ROUTE_ORDERS_SCREEN = "orders"


fun NavGraphBuilder.ordersListScreen(
    rootRoute: DestinationRoute,
    onOrderClick: (rootRoute: DestinationRoute, orderId: Int) -> Unit,
    onBack: () -> Unit,
) {
    composable(
        route = "${rootRoute.route}/${ROUTE_ORDERS_SCREEN}",
    ) { _ ->
        val viewModel = koinInject<OrderListViewModel>()

        DisposableEffect(viewModel) {
            onDispose { viewModel.clear() }
        }

        OrderListScreen(
            onOrderClick = { onOrderClick(rootRoute, it) },
            onBack = onBack,
            viewModel = viewModel
        )
    }
}

fun NavController.navigateOrdersList(
    rootRoute: DestinationRoute
) {
    navigate("${rootRoute.route}/${ROUTE_ORDERS_SCREEN}")
}
