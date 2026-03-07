package com.solutionium.woo.ui.navigation

import com.solutionium.sharedui.common.DestinationRoute
import com.solutionium.feature.account.GRAPH_ACCOUNT_ROUTE
import com.solutionium.feature.cart.GRAPH_CART_ROUTE
import com.solutionium.feature.category.GRAPH_CATEGORY_ROUTE
import com.solutionium.feature.home.GRAPH_HOME_ROUTE


sealed class RootScreen(val destinationRoute: DestinationRoute) {
    data object Home : RootScreen(GRAPH_HOME_ROUTE)
    data object Category : RootScreen(GRAPH_CATEGORY_ROUTE)
    data object Cart : RootScreen(GRAPH_CART_ROUTE)
    data object Account : RootScreen(GRAPH_ACCOUNT_ROUTE)
}