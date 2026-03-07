package com.solutionium.shared.viewmodel

import com.solutionium.shared.domain.config.getConfigDomainModules
import com.solutionium.shared.domain.products.getProductsDomainModules
import com.solutionium.shared.domain.user.getUserDomainModules
import org.koin.dsl.module

fun getCategoryModules() =
    setOf(categoryModule) +
        getProductsDomainModules() +
        getUserDomainModules() +
        getConfigDomainModules()

val categoryModule = module {
    factory {
        CategoryViewModel(
            getBrands = get(),
            getAttributeTerms = get(),
            getAppImages = get(),
            getSearchTabs = get(),
            searchProducts = get(),
            checkSuperUserUserCase = get(),
        )
    }
}
