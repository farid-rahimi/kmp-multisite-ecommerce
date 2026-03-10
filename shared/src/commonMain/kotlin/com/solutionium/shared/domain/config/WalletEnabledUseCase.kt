package com.solutionium.shared.domain.config

interface WalletEnabledUseCase {
    suspend operator fun invoke(): Boolean
}

