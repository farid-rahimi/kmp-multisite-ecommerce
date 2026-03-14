package com.solutionium.shared.domain.config.impl

import com.solutionium.shared.data.config.AppConfigRepository
import com.solutionium.shared.data.network.NetworkConfigProvider
import com.solutionium.shared.data.products.WooProductRepository
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.domain.config.ReviewCriteriaUseCase
import com.solutionium.shared.util.currentLanguageCode

class ReviewCriteriaUseCaseImpl(
    private val appConfigRepository: AppConfigRepository,
    private val productRepository: WooProductRepository,
    private val networkConfigProvider: NetworkConfigProvider,
) : ReviewCriteriaUseCase {
    override suspend fun invoke(productId: Int, catIds: List<Int>): List<String> {
        val appConfigResult = appConfigRepository.getAppConfig()
        val configuredCriteriaPath = if (appConfigResult is Result.Success) {
            appConfigResult.data.reviewCriteriaEndpoint?.trim()?.takeIf { it.isNotBlank() }
        } else {
            null
        }

        val remoteCriteriaPath = configuredCriteriaPath ?: networkConfigProvider.get().reviewCriteriaPath.orEmpty()
        val languageCode = currentLanguageCode()
        if (remoteCriteriaPath.isNotBlank()) {
            when (
                val result = productRepository.getReviewCriteria(
                    productId = productId,
                    categoryIds = catIds,
                    criteriaPathOverride = remoteCriteriaPath,
                    languageCode = languageCode,
                )
            ) {
                is Result.Success -> if (result.data.isNotEmpty()) return result.data
                is Result.Failure -> Unit
            }
        }

        return when (appConfigResult) {
            is Result.Success -> {
                appConfigResult.data.reviewCriteria.find { it.catID in catIds }?.criteria ?: emptyList()
            }
            is Result.Failure -> {
                emptyList()
            }
        }
    }
}
