package com.solutionium.woo.ui.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.solutionium.sharedui.common.DestinationRoute
import com.solutionium.shared.data.model.StoryItem
import com.solutionium.feature.account.accountScreen
import com.solutionium.feature.account.navigateToAccount
import com.solutionium.feature.address.addressScreen
import com.solutionium.feature.address.navigateAddress
import com.solutionium.feature.address.navigateAddressList
import com.solutionium.feature.cart.cartScreen
import com.solutionium.feature.category.categoryScreen
import com.solutionium.feature.checkout.checkoutScreen
import com.solutionium.feature.checkout.navigateCheckout
import com.solutionium.feature.home.GRAPH_HOME_ROUTE
import com.solutionium.shared.viewmodel.HomeViewModel
import com.solutionium.feature.home.homeScreen
import com.solutionium.feature.orders.navigateOrdersList
import com.solutionium.feature.orders.ordersListScreen
import com.solutionium.feature.product.detail.navigateProductDetail
import com.solutionium.feature.product.detail.productDetailScreen
import com.solutionium.feature.product.list.navigateProductList
import com.solutionium.feature.product.list.productListScreen
import com.solutionium.feature.review.navigateReviews
import com.solutionium.feature.review.reviewScreen

@Composable
fun WooNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onStoryClick: (StoryItem) -> Unit,
    homeViewModel: HomeViewModel
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = GRAPH_HOME_ROUTE.route,
    ) {

        homeScreen(
            onProductClick = navController::navigateProductDetail,
            onShowProductListClick = navController::navigateProductList,
            onStoryClick = onStoryClick,
            homeViewModel = homeViewModel,
            nestedGraphs = {rootRoute ->
                productListScreen(rootRoute, navController)
                productDetailScreen(rootRoute, navController)
                reviewsScreen(rootRoute, navController)
            }
        )

        categoryScreen(
            navigateToProductList = navController::navigateProductList,
            onNavigateBack = navController::popBackStack,
            onProductClick = navController::navigateProductDetail,
            nestedGraphs = {rootRoute ->
                productListScreen(rootRoute, navController)
                productDetailScreen(rootRoute, navController)
                reviewsScreen(rootRoute, navController)
            }
        )

        cartScreen(
            onCheckoutClick = navController::navigateCheckout,
            onProductClick = navController::navigateProductDetail,
            onNavigateAccount = navController::navigateToAccount,
            nestedGraphs = {rootRoute ->
                checkoutScreen(rootRoute, navController)
                addressScreen(rootRoute, navController)
                productListScreen(rootRoute, navController)
                productDetailScreen(rootRoute, navController)
                reviewsScreen(rootRoute, navController)

            }
        )

        accountScreen(
            onAddressClick = navController::navigateAddressList,
            navigateToProductList = navController::navigateProductList,
            onOrdersClick = navController::navigateOrdersList,
            onOrderClick = { _,_-> }, //navController::navigateOrderDetails,
            onBack = navController::popBackStack,
            nestedGraphs = {rootRoute ->
                addressScreen(rootRoute, navController)
                productListScreen(rootRoute, navController)
                productDetailScreen(rootRoute, navController)
                reviewsScreen(rootRoute, navController)
                ordersListScreen(rootRoute, navController)
            }
        )


    }
}

private fun NavGraphBuilder.productListScreen(
    rootRoute: DestinationRoute,
    navController: NavHostController,
) {
    productListScreen(
        rootRoute = rootRoute,
        onProductClick = navController::navigateProductDetail,
        onBack = navController::popBackStack,
    )
}

private fun NavGraphBuilder.productDetailScreen(
    rootRoute: DestinationRoute,
    navController: NavHostController,
) {
    productDetailScreen(
        rootRoute = rootRoute,
        onAllReviewClicked = navController::navigateReviews,
        navigateToProductList = navController::navigateProductList,
        onBackClick = navController::popBackStack,
    )
}

private fun NavGraphBuilder.reviewsScreen(
    rootRoute: DestinationRoute,
    navController: NavHostController,
) {
    reviewScreen(
        rootRoute = rootRoute,
        onBackClick = navController::popBackStack
    )
}

private fun NavGraphBuilder.ordersListScreen(
    rootRoute: DestinationRoute,
    navController: NavHostController,
) {
    ordersListScreen(
        rootRoute = rootRoute,
        onOrderClick = {_,_->},//navController::navigateOrderDetails,
        onBack = navController::popBackStack
    )
}

private fun NavGraphBuilder.checkoutScreen(
    rootRoute: DestinationRoute,
    navController: NavHostController,
) {
    checkoutScreen(
        rootRoute = rootRoute,
        onConfirmSuccessOrder = {
            navController.navigate(GRAPH_HOME_ROUTE.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
            }
        },
        onBack = navController::popBackStack,
        onNavigateToAccount = navController::navigateToAccount,
        onAddEditAddress = navController::navigateAddress
    )
}

private fun NavGraphBuilder.addressScreen(
    rootRoute: DestinationRoute,
    navController: NavHostController,
) {
    addressScreen(
        rootRoute = rootRoute,
        onConfirm = navController::popBackStack,
        onBack = navController::popBackStack,
        onAddEditAddress = { navController.navigateAddress(rootRoute, it) }
    )
}