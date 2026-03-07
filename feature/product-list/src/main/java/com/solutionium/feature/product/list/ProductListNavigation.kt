package com.solutionium.feature.product.list

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.solutionium.sharedui.products.ProductListScreen
import com.solutionium.sharedui.common.DestinationRoute
import com.solutionium.shared.data.model.PRODUCT_ARG_ATTRIBUTE
import com.solutionium.shared.data.model.PRODUCT_ARG_ATTRIBUTE_TERM
import com.solutionium.shared.data.model.PRODUCT_ARG_BRAND_ID
import com.solutionium.shared.data.model.PRODUCT_ARG_CATEGORY
import com.solutionium.shared.data.model.PRODUCT_ARG_FEATURED
import com.solutionium.shared.data.model.PRODUCT_ARG_IDS
import com.solutionium.shared.data.model.PRODUCT_ARG_ON_SALE
import com.solutionium.shared.data.model.PRODUCT_ARG_SEARCH
import com.solutionium.shared.data.model.PRODUCT_ARG_TAG
import com.solutionium.shared.data.model.PRODUCT_ARG_TITLE

internal const val PRODUCT_LIST_ROUTE = "productList"
internal const val PRODUCT_ARG_LIST_TYPE = "listType"


fun NavGraphBuilder.productListScreen(
    rootRoute: DestinationRoute,
    onProductClick: (rootRoute: DestinationRoute, id: Int) -> Unit,
    onBack: () -> Unit,
) {
    composable(
        route = "${rootRoute.route}/${PRODUCT_LIST_ROUTE}" +
                "?${PRODUCT_ARG_BRAND_ID}={${PRODUCT_ARG_BRAND_ID}}" +
                "&${PRODUCT_ARG_ATTRIBUTE}={${PRODUCT_ARG_ATTRIBUTE}}" +
                "&${PRODUCT_ARG_ATTRIBUTE_TERM}={${PRODUCT_ARG_ATTRIBUTE_TERM}}" +
                "&${PRODUCT_ARG_IDS}={${PRODUCT_ARG_IDS}}" +
                "&${PRODUCT_ARG_TITLE}={${PRODUCT_ARG_TITLE}}" +
                "&${PRODUCT_ARG_CATEGORY}={${PRODUCT_ARG_CATEGORY}}" +
                "&${PRODUCT_ARG_TAG}={${PRODUCT_ARG_TAG}}" +
                "&${PRODUCT_ARG_SEARCH}={${PRODUCT_ARG_SEARCH}}" +
                "&${PRODUCT_ARG_FEATURED}={${PRODUCT_ARG_FEATURED}}" +
                "&${PRODUCT_ARG_ON_SALE}={${PRODUCT_ARG_ON_SALE}}"
        ,
        arguments = listOf(
            navArgument(PRODUCT_ARG_BRAND_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(PRODUCT_ARG_ATTRIBUTE) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(PRODUCT_ARG_ATTRIBUTE_TERM) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(PRODUCT_ARG_IDS) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(PRODUCT_ARG_TITLE) {
                type = NavType.StringType
                nullable = true // Make it optional
                defaultValue = null
            },
            navArgument(PRODUCT_ARG_CATEGORY) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(PRODUCT_ARG_TAG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(PRODUCT_ARG_SEARCH) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(PRODUCT_ARG_FEATURED) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(PRODUCT_ARG_ON_SALE) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        )
    ) { navBack ->
        val routeArgs = listOf(
            PRODUCT_ARG_BRAND_ID,
            PRODUCT_ARG_ATTRIBUTE,
            PRODUCT_ARG_ATTRIBUTE_TERM,
            PRODUCT_ARG_IDS,
            PRODUCT_ARG_TITLE,
            PRODUCT_ARG_CATEGORY,
            PRODUCT_ARG_TAG,
            PRODUCT_ARG_SEARCH,
            PRODUCT_ARG_FEATURED,
            PRODUCT_ARG_ON_SALE,
        ).mapNotNull { key -> navBack.arguments?.getString(key)?.let { key to it } }.toMap()

        ProductListScreen(
            onProductClick = { onProductClick(rootRoute, it) },
            onBack = onBack,
            args = routeArgs,
        )
    }
}

//fun NavController.navigateProductList(
//    rootRoute: DestinationRoute,
//    productType: ProductListType,
//) {
//    val route = rootRoute.route
//
//    navigate(route)
//}

fun NavController.navigateProductList(
    rootRoute: DestinationRoute,
    params: Map<String, String>
) {
    var route = "${rootRoute.route}/${PRODUCT_LIST_ROUTE}"
    val queryParams = mutableListOf<String>()

    params.map { (key, value) -> queryParams.add("$key=$value") }

    if (queryParams.isNotEmpty()) {
        route += "?" + queryParams.joinToString("&")
    }
    navigate(route)
}

