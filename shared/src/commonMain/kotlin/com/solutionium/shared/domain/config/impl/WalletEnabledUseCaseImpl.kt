package com.solutionium.shared.domain.config.impl

import com.solutionium.shared.data.config.AppConfigRepository
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.domain.config.WalletEnabledUseCase

class WalletEnabledUseCaseImpl(
    private val appConfigRepository: AppConfigRepository,
) : WalletEnabledUseCase {
    override suspend fun invoke(): Boolean =
        when (val result = appConfigRepository.getAppConfig()) {
            is Result.Success -> result.data.walletEnabled
            is Result.Failure -> false
        }
}

