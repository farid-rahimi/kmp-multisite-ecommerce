package com.solutionium.feature.review

import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.solutionium.sharedui.common.DestinationRoute
import com.solutionium.sharedui.review.ReviewListScreen
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf


fun NavGraphBuilder.reviewScreen(
    rootRoute: DestinationRoute,
    onBackClick: () -> Unit,
) {
    composable(
        route = "${rootRoute.route}/reviews?id={productId}&catIds={categoryIds}",
        arguments = listOf(
            navArgument("productId") {
                type = NavType.IntType
                defaultValue = -1
            },
            navArgument("categoryIds") {
                type = NavType.StringType
                nullable = true // Make it nullable in case it's not passed
            }
        ),

        ) { navBackStackEntry ->
        val args = buildMap {
            put("productId", navBackStackEntry.arguments?.getInt("productId", -1).toString())
            navBackStackEntry.arguments?.getString("categoryIds")?.let { put("categoryIds", it) }
        }
        val viewModel = koinInject<com.solutionium.shared.viewmodel.ReviewViewModel>(
            parameters = { parametersOf(args) },
        )
        DisposableEffect(viewModel) {
            onDispose { viewModel.clear() }
        }

        ReviewListScreen(
            viewModel = viewModel,
            onBackClick = onBackClick
        )
    }
}

fun NavController.navigateReviews(
    rootRoute: DestinationRoute,
    productId: Int,
    categoryIds: List<Int>
) {
    val categoryIdsArg = categoryIds.joinToString(",")
    navigate("${rootRoute.route}/reviews?id=$productId&catIds=$categoryIdsArg")
}
