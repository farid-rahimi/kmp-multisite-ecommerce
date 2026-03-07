package com.solutionium.shared.viewmodel

import com.solutionium.shared.domain.cart.getCartDomainModules
import com.solutionium.shared.domain.config.getConfigDomainModules
import com.solutionium.shared.domain.user.getUserDomainModules
import org.koin.dsl.module

fun getCartModules() =
    getCartDomainModules() +
        setOf(cartModule) +
        getUserDomainModules() +
        getConfigDomainModules()

val cartModule = module {
    factory {
        CartViewModel(
            updateCartItemUseCase = get(),
            observeCartUseCase = get(),
            clearCartUseCase = get(),
            validateCartUseCase = get(),
            confirmValidation = get(),
            paymentMethodDiscountUseCase = get(),
            checkLoginUserUseCase = get(),
        )
    }
}
