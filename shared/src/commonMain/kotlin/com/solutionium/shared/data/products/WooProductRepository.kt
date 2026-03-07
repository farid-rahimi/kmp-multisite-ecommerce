package com.solutionium.shared.data.products

import androidx.paging.PagingData
import com.solutionium.shared.data.model.AttributeTerm
import com.solutionium.shared.data.model.AttributeTermsListType
import com.solutionium.shared.data.model.Brand
import com.solutionium.shared.data.model.BrandListType
import com.solutionium.shared.data.model.CartItemServer
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.NewReview
import com.solutionium.shared.data.model.ProductDetail
import com.solutionium.shared.data.model.ProductThumbnail
import com.solutionium.shared.data.model.ProductVariation
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.model.Review
import kotlinx.coroutines.flow.Flow

interface WooProductRepository {

    suspend fun getProductDetails(productId: Int): Result<ProductDetail, GeneralError>
    suspend fun getProductDetails(slug: String): Result<ProductDetail, GeneralError>


    suspend fun getProductsList(
        queries: Map<String, String>
    ): Result<List<ProductThumbnail>, GeneralError>

    fun getProductListStream(
        queries: Map<String, String>
    ): Flow<PagingData<ProductThumbnail>>

    suspend fun getReviewList(
        queries: Map<String, String>
    ): Result<List<Review>, GeneralError>

    fun getReviewListStream(
        queries: Map<String, String>
    ): Flow<PagingData<Review>>

    suspend fun getBrandList(type: BrandListType): Result<List<Brand>, GeneralError>
    suspend fun getBrandList(queries: Map<String, String>): Result<List<Brand>, GeneralError>

    suspend fun getAttributeTerms(listType: AttributeTermsListType): Result<List<AttributeTerm>, GeneralError>
    suspend fun getAttributeTerms(
        attributeId: Int,
        queries: Map<String, String>,
    ): Result<List<AttributeTerm>, GeneralError>
    //suspend fun getProductDetailsForValidation(productIds: List<Int>): Result<List<ProductDetail>, GeneralError>

    suspend fun getCartUpdateServer(productIds: List<Int>): Result<List<CartItemServer>, GeneralError>
    suspend fun getProductVariations(productId: Int): Result<List<ProductVariation>, GeneralError>
    suspend fun submitReview(review: NewReview): Result<Review, GeneralError>
}
