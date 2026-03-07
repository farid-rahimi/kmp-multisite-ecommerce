package com.solutionium.shared.viewmodel

import com.solutionium.shared.domain.config.getConfigDomainModules
import com.solutionium.shared.domain.favorite.getFavoriteDomainModules
import com.solutionium.shared.domain.order.getOrderDomainModules
import com.solutionium.shared.domain.user.getUserDomainModules
import org.koin.dsl.module

fun getAccountModules() =
    setOf(accountModule) +
        getUserDomainModules() +
        getFavoriteDomainModules() +
        getConfigDomainModules() +
        getOrderDomainModules()

val accountModule = module {
    factory {
        AccountViewModel(
            checkLoginUserUseCase = get(),
            sendOtpUseCase = get(),
            loginOrRegisterUseCase = get(),
            loginByUserPassUseCase = get(),
            logoutUseCase = get(),
            getCurrentUserUseCase = get(),
            editUserDetailsUseCase = get(),
            getUserWalletUseCase = get(),
            observeFavoritesUseCase = get(),
            latestOrderUseCase = get(),
            seLanguageUseCase = get(),
            observeLanguageUseCase = get(),
            getPrivacyPolicyUseCase = get(),
            getContactInfoUseCase = get()
        )
    }
}
