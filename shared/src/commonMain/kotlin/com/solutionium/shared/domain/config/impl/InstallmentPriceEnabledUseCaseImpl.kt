package com.solutionium.shared.domain.config.impl

import com.solutionium.shared.data.config.AppConfigRepository
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.domain.config.InstallmentPriceEnabledUseCase

class InstallmentPriceEnabledUseCaseImpl(
    private val appConfigRepository: AppConfigRepository,
) : InstallmentPriceEnabledUseCase {
    override suspend fun invoke(): Boolean =
        when (val result = appConfigRepository.getAppConfig()) {
            is Result.Success -> result.data.installmentPrice
            is Result.Failure -> false
        }
}
