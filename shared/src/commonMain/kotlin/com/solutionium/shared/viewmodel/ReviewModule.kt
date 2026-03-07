package com.solutionium.shared.viewmodel

import com.solutionium.shared.domain.config.getConfigDomainModules
import com.solutionium.shared.domain.review.getReviewDomainModules
import com.solutionium.shared.domain.user.getUserDomainModules
import org.koin.dsl.module

fun getReviewModules() =
    setOf(reviewModule) + getReviewDomainModules() + getUserDomainModules() + getConfigDomainModules()

val reviewModule = module {
    factory { params ->
        val args: Map<String, String> = params.getOrNull() ?: emptyMap()
        ReviewViewModel(
            initialArgs = args,
            getReviewListPaging = get(),
            submitReviewUseCase = get(),
            checkLoginUserUseCase = get(),
            getCurrentUserUseCase = get(),
            reviewCriteriaUseCase = get(),
        )
    }
}
