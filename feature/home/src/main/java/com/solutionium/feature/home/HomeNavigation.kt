package com.solutionium.feature.home

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.solutionium.sharedui.common.DestinationRoute
import com.solutionium.sharedui.home.HomeScreen
import com.solutionium.shared.data.model.StoryItem
import com.solutionium.shared.viewmodel.HomeViewModel

val GRAPH_HOME_ROUTE = DestinationRoute("home_graph_route")
private const val ROUTE_HOME_SCREEN = "home"


fun NavGraphBuilder.homeScreen(
    onProductClick: (rootRoute: DestinationRoute, id: Int) -> Unit,
    onShowProductListClick: (rootRoute: DestinationRoute, params: Map<String, String>) -> Unit,
    onStoryClick: (StoryItem) -> Unit,
    nestedGraphs: NavGraphBuilder.(DestinationRoute) -> Unit,
    homeViewModel: HomeViewModel
) {
    navigation(
        route = GRAPH_HOME_ROUTE.route,
//        deepLinks = listOf(navDeepLink {
//            uriPattern = "https://qeshminora.com/product/{productSlug}"
//            //action = Intent.ACTION_VIEW
//        }),
        startDestination = "${GRAPH_HOME_ROUTE.route}/$ROUTE_HOME_SCREEN",
    ) {
        composable(
            "${GRAPH_HOME_ROUTE.route}/$ROUTE_HOME_SCREEN",
        ) { navBackStack ->
            val context = LocalContext.current
            //val productSlug = navBackStack.arguments?.getString("productSlug")
            HomeScreen(
                onProductClick = { onProductClick(GRAPH_HOME_ROUTE, it) },
                navigateToProductList = { params -> onShowProductListClick(GRAPH_HOME_ROUTE, params) },
                onStoryClick = onStoryClick,
                viewModel = homeViewModel,
                onUpdateNowClick = {
                    val packageName = context.packageName
                    val marketIntent = Intent(
                        Intent.ACTION_VIEW,
                        "market://details?id=$packageName".toUri()
                    )
                    val webIntent = Intent(
                        Intent.ACTION_VIEW,
                        "https://play.google.com/store/apps/details?id=$packageName".toUri()
                    )
                    runCatching { context.startActivity(marketIntent) }
                        .onFailure { context.startActivity(webIntent) }
                }
            )
        }

        nestedGraphs(GRAPH_HOME_ROUTE)
    }
}

fun NavController.navigateToHome() {
    navigate(GRAPH_HOME_ROUTE.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
