package com.solutionium.shared.domain.config

interface ReviewCriteriaUseCase {

    suspend operator fun invoke(productId: Int, catIds: List<Int>): List<String>

}
