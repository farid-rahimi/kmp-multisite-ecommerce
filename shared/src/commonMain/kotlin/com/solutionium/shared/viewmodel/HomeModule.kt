package com.solutionium.shared.viewmodel

import com.solutionium.shared.domain.cart.getCartDomainModules
import com.solutionium.shared.domain.categories.getCategoryDomainModules
import com.solutionium.shared.domain.config.getConfigDomainModules
import com.solutionium.shared.domain.favorite.getFavoriteDomainModules
import com.solutionium.shared.domain.products.getProductsDomainModules
import com.solutionium.shared.domain.user.getUserDomainModules
import org.koin.dsl.module

fun getHomeModules() =
    setOf(homeModule) +
        getCartDomainModules() +
        getFavoriteDomainModules() +
        getUserDomainModules() +
        getConfigDomainModules() +
        getProductsDomainModules() +
        getCategoryDomainModules()

val homeModule = module {
    factory {
        HomeViewModel(
            getProductsUseCase = get(),
            getCategoriesUseCase = get(),
            observeCartUseCase = get(),
            addToCart = get(),
            updateCartItem = get(),
            observeFavoritesUseCase = get(),
            toggleFavoriteUseCase = get(),
            homeBannerUseCase = get(),
            paymentMethodDiscountUseCase = get(),
            getStoriesUseCase = get(),
            addStoryViewUseCase = get(),
            getAllViewedStories = get(),
            getHeaderLogoUseCase = get(),
            checkLoginUserUseCase = get(),
            checkSuperUserUseCase = get(),
            getVersionsUseCase = get(),
            getContactInfoUseCase = get(),
            appVersionProvider = get(),
            observeLanguageUseCase = get(),
        )
    }
}
