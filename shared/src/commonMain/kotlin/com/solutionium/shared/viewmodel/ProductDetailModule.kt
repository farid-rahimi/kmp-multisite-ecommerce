package com.solutionium.shared.viewmodel

import com.solutionium.shared.domain.cart.getCartDomainModules
import com.solutionium.shared.domain.config.getConfigDomainModules
import com.solutionium.shared.domain.favorite.getFavoriteDomainModules
import com.solutionium.shared.domain.products.getProductsDomainModules
import com.solutionium.shared.domain.review.getReviewDomainModules
import org.koin.dsl.module

fun getProductDetailModules() =
    setOf(productDetailModule) +
        getCartDomainModules() +
        getProductsDomainModules() +
        getFavoriteDomainModules() +
        getReviewDomainModules() +
        getConfigDomainModules()

val productDetailModule = module {
    factory { params ->
        val args: Map<String, String> = params.getOrNull() ?: emptyMap()
        ProductDetailViewModel(
            initialArgs = args,
            getProductDetails = get(),
            getProductVariations = get(),
            addToCart = get(),
            updateCartItemUseCase = get(),
            getProductInCartQuantity = get(),
            observeFavoritesUseCase = get(),
            toggleFavoriteUseCase = get(),
            paymentMethodDiscountUseCase = get(),
            installmentPriceEnabledUseCase = get(),
            getTopReviews = get(),
        )
    }
}
