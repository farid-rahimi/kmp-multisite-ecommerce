package com.solutionium.shared.domain.config.impl

import com.solutionium.shared.data.config.AppConfigRepository
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.model.SearchTabConfig
import com.solutionium.shared.domain.config.GetSearchTabsUseCase

internal class GetSearchTabsUseCaseImpl(
    private val configRepository: AppConfigRepository,
) : GetSearchTabsUseCase {
    override suspend fun invoke(): List<SearchTabConfig> =
        when (val result = configRepository.getAppConfig()) {
            is Result.Success -> result.data.searchTabs
            is Result.Failure -> emptyList()
        }
}

