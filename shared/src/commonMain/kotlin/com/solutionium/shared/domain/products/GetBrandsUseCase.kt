package com.solutionium.shared.domain.products

import com.solutionium.shared.data.model.Brand
import com.solutionium.shared.data.model.BrandListType
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Result
import kotlinx.coroutines.flow.Flow

interface GetBrandsUseCase {
    suspend operator fun invoke(type: BrandListType): Flow<Result<List<Brand>, GeneralError>>
    suspend operator fun invoke(queries: Map<String, String>): Flow<Result<List<Brand>, GeneralError>>
}
