package com.solutionium.shared.domain.config.impl

import com.solutionium.shared.data.config.AppConfigRepository
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.domain.config.HeaderLogoConfig
import com.solutionium.shared.domain.config.GetHeaderLogoUseCase

class GetHeaderLogoUseCaseImpl(
    private val configRepository: AppConfigRepository
) : GetHeaderLogoUseCase {
    override suspend fun invoke(): HeaderLogoConfig? =
        when (val result = configRepository.getAppConfig()) {
            is Result.Success -> {
                HeaderLogoConfig(
                    lightUrl = result.data.headerLogoLightUrl ?: result.data.headerLogoUrl,
                    darkUrl = result.data.headerLogoDarkUrl ?: result.data.headerLogoUrl,
                )
            }

            is Result.Failure -> {
                null
            }
        }
}
