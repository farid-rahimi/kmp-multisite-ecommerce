package com.solutionium.shared.viewmodel

import com.solutionium.shared.domain.user.getUserDomainModules
import org.koin.dsl.module

fun getAddressModules() = setOf(addressModule) + getUserDomainModules()

val addressModule = module {
    factory { (args: Map<String, String>) ->
        AddressViewModel(
            saveAddressUseCase = get(),
            loadAddressUseCase = get(),
            deleteAddressUseCase = get(),
            setAsDefaultAddressUseCase = get(),
            args = args,
        )
    }
}
