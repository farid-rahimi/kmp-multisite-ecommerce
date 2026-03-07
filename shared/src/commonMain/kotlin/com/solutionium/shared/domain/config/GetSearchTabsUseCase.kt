package com.solutionium.shared.domain.config

import com.solutionium.shared.data.model.SearchTabConfig

interface GetSearchTabsUseCase {
    suspend operator fun invoke(): List<SearchTabConfig>
}

