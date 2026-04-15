package com.solutionium.shared.domain.config

interface GetHeaderLogoUseCase {

    suspend operator fun invoke(): HeaderLogoConfig?

}

data class HeaderLogoConfig(
    val lightUrl: String?,
    val darkUrl: String?,
)
