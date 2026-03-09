package com.solutionium.shared.domain.config

interface InstallmentPriceEnabledUseCase {
    suspend operator fun invoke(): Boolean
}
