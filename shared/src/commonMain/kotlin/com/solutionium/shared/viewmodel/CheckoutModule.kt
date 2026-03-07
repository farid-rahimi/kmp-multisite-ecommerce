package com.solutionium.shared.viewmodel

import com.solutionium.shared.domain.cart.getCartDomainModules
import com.solutionium.shared.domain.checkout.getCheckoutDomainModules
import com.solutionium.shared.domain.config.getConfigDomainModules
import com.solutionium.shared.domain.user.getUserDomainModules
import org.koin.dsl.module

fun getCheckoutModules() =
    setOf(checkoutModule) +
        getCartDomainModules() +
        getCheckoutDomainModules() +
        getUserDomainModules() +
        getConfigDomainModules()

val checkoutModule = module {
    factory {
        CheckoutViewModel(
            observeCartUseCase = get(),
            getShippingMethodsUseCase = get(),
            getForcedEnabledPayment = get(),
            getPaymentGatewaysUseCase = get(),
            loadAddressesUseCase = get(),
            applyCouponUseCase = get(),
            createOrderUseCase = get(),
            clearCartUseCase = get(),
            getOrderStatusUseCase = get(),
            paymentMethodDiscountUseCase = get(),
            getBACSDetails = get(),
            getUserWalletUseCase = get(),
        )
    }
}
