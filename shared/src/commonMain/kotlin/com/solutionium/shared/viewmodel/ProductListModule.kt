package com.solutionium.shared.viewmodel

import com.solutionium.shared.domain.cart.getCartDomainModules
import com.solutionium.shared.domain.config.getConfigDomainModules
import com.solutionium.shared.domain.favorite.getFavoriteDomainModules
import com.solutionium.shared.domain.user.getUserDomainModules
import com.solutionium.shared.domain.products.getProductsDomainModules
import org.koin.dsl.module

fun getProductListModules() = setOf(productListModule) + getProductsDomainModules() + getCartDomainModules() + getFavoriteDomainModules() + getConfigDomainModules() + getUserDomainModules()

val productListModule = module {
    factory { params ->
        val args: Map<String, String> = params.getOrNull() ?: emptyMap()
        ProductListViewModel(
            initialArgs = args,
            productList = get(),
            observeCartUseCase = get(),
            addToCart = get(),
            updateCartItem = get(),
            observeFavoritesUseCase = get(),
            toggleFavoriteUseCase = get(),
            paymentMethodDiscountUseCase = get(),
            checkSuperUserUserCase = get()
        )
    }
}
