package com.solutionium.shared.domain.products.impl

import com.solutionium.shared.data.model.AttributeTermsListType
import com.solutionium.shared.data.products.WooProductRepository
import com.solutionium.shared.domain.products.GetAttributeTermsUseCase
import kotlinx.coroutines.flow.flow

internal class GetAttributeTermsUseCaseImpl(
    private val wooProductRepository: WooProductRepository
) : GetAttributeTermsUseCase {
    override suspend fun invoke(listType: AttributeTermsListType) = flow {
        emit(wooProductRepository.getAttributeTerms(listType))
    }

    override suspend fun invoke(
        attributeId: Int,
        queries: Map<String, String>,
    ) = flow {
        emit(wooProductRepository.getAttributeTerms(attributeId, queries))
    }
}
