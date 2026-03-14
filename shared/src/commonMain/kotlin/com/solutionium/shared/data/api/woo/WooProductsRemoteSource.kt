package com.solutionium.shared.data.api.woo

import com.solutionium.shared.data.model.AttributeTerm
import com.solutionium.shared.data.model.Brand
import com.solutionium.shared.data.model.CartItemServer
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.NewReview
import com.solutionium.shared.data.model.ProductDetail
import com.solutionium.shared.data.model.ProductThumbnail
import com.solutionium.shared.data.model.ProductVariation
import com.solutionium.shared.data.model.Review
import com.solutionium.shared.data.model.Result


interface WooProductsRemoteSource {

    suspend fun getProductDetails(productId: Int): Result<ProductDetail, GeneralError>
    suspend fun getProductDetails(slug: String): Result<ProductDetail, GeneralError>

    suspend fun getProductList(
        page: Int,
        queries: Map<String, String>
    ): Result<List<ProductThumbnail>, GeneralError>

    suspend fun getBrandList(
        queries: Map<String, String>
    ): Result<List<Brand>, GeneralError>

    suspend fun getCartUpdateServer(
        queries: Map<String, String>
    ): Result<List<CartItemServer>, GeneralError>

    suspend fun getAttributeTerms(
        attributeId: Int,
        queries: Map<String, String>
    ): Result<List<AttributeTerm>, GeneralError>
//
//    suspend fun getProductDetailsListById(
//        productIds: List<Int>
//    ): Result<List<ProductDetail>, GeneralError>

    suspend fun getProductVariations(productId: Int): Result<List<ProductVariation>, GeneralError>


    companion object {
        const val PAGE_SIZE = 20 // WOO API default page size
    }

    suspend fun getProductReviews(page: Int, queries: Map<String, String>): Result<List<Review>, GeneralError>
    suspend fun getReviewCriteria(
        productId: Int,
        categoryIds: List<Int>,
        criteriaPathOverride: String? = null,
        languageCode: String? = null,
    ): Result<List<String>, GeneralError>
    suspend fun submitReview(review: NewReview): Result<Review, GeneralError>
}
