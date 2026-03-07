package com.solutionium.feature.checkout

import org.koin.compose.viewmodel.koinViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.solutionium.sharedui.common.DestinationRoute

private const val ROUTE_CHECKOUT_SCREEN = "checkout"

object CheckoutDestinations {
    const val CHECKOUT_MAIN = "checkout_main"
    const val ORDER_STATUS = "order_status"
}

object OrderStatusDestinations {
    const val PLACING_ORDER = "placing_order"
    const val ORDER_SUCCESS = "order_success"
    const val ORDER_FAILED = "order_failed"
}

fun NavGraphBuilder.checkoutScreen(
    rootRoute: DestinationRoute,
    onConfirmSuccessOrder: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onBack: () -> Unit,
    onAddEditAddress: (rootRoute: DestinationRoute, addressId: Int?) -> Unit,
) {
    composable(
        route = "${rootRoute.route}/${ROUTE_CHECKOUT_SCREEN}",
//        deepLinks = listOf(
//            navDeepLink { uriPattern = "solutionium://payment-return?status={status}&order_id={order_id}" }
//        )
    ) { navBackStackEntry ->
        // Extract the status from the deep link arguments
        val status = navBackStackEntry.arguments?.getString("status")
        val orderId = navBackStackEntry.arguments?.getInt("order_id") ?: 0

        CheckoutScreen(

            onBack = onBack,
            onAddEditAddressClick = { onAddEditAddress(rootRoute, it) },
            onContinueShopping = onConfirmSuccessOrder,
            paymentReturnStatus = status,
            paymentReturnOrderId = orderId,
            viewModel =koinViewModel()
        )
    }
}

fun NavController.navigateCheckout(
    rootRoute: DestinationRoute
) {
    navigate("${rootRoute.route}/${ROUTE_CHECKOUT_SCREEN}")
}

