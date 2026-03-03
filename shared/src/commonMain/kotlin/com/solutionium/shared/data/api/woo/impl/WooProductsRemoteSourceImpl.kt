package com.solutionium.shared.data.api.woo.impl

import com.solutionium.shared.data.api.woo.converters.toModel
import com.solutionium.shared.data.api.woo.converters.toRequestBody
import com.solutionium.shared.data.model.AttributeTerm
import com.solutionium.shared.data.model.Brand
import com.solutionium.shared.data.model.CartItemServer
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.NewReview
import com.solutionium.shared.data.model.ProductDetail
import com.solutionium.shared.data.model.ProductThumbnail
import com.solutionium.shared.data.model.ProductVariation
import com.solutionium.shared.data.model.Review
import com.solutionium.shared.data.api.woo.WooProductsRemoteSource
import com.solutionium.shared.data.api.woo.converters.toAttributeTerm
import com.solutionium.shared.data.api.woo.converters.toBrand
import com.solutionium.shared.data.api.woo.converters.toProductDetail
import com.solutionium.shared.data.api.woo.converters.toProductThumbnail
import com.solutionium.shared.data.api.woo.handleNetworkResponse
import com.solutionium.shared.data.network.adapter.NetworkResponse
import com.solutionium.shared.data.network.clients.WooProductClient
import com.solutionium.shared.data.network.response.CartCheckListResponse
import com.solutionium.shared.data.model.Result



internal class WooProductsRemoteSourceImpl(

    private val productApi: WooProductClient
) : WooProductsRemoteSource {
    override suspend fun getProductDetails(productId: Int): Result<ProductDetail, GeneralError> =
        handleNetworkResponse(
            networkCall = { productApi.getProductDetails(productId) },
            mapper = { response ->
                response.toProductDetail()
            }
        )

    override suspend fun getProductDetails(slug: String): Result<ProductDetail, GeneralError> =
        handleNetworkResponse(
            networkCall = { productApi.getProductList(queries = mapOf("slug" to slug)) },
            mapper = { response ->
                response.first().toProductDetail()
            }
        )


    override suspend fun getProductList(
        page: Int,
        queries:  Map<String, String>,
    ): Result<List<ProductThumbnail>, GeneralError> =
        handleNetworkResponse(
            networkCall = { productApi.getProductList(page, queries) },
            //networkCall = { productApi.getFastProduct(page, queries) },
            mapper = { responseList ->
                responseList.map { it.toProductThumbnail() }
                //responseList.products?.map { it.toProductThumbnail() } ?: emptyList()
            }
        )

    override suspend fun getProductVariations(
        productId: Int
    ): Result<List<ProductVariation>, GeneralError> =
        handleNetworkResponse(
            networkCall = { productApi.getProductVariations(productId) },
            mapper = { responseList ->
                responseList.map { it.toModel() }
            }
        )

    override suspend fun getBrandList(
        queries: Map<String, String>
    ): Result<List<Brand>, GeneralError> =
        handleNetworkResponse(
            networkCall = { productApi.getProductBrands(queries) },
            mapper = { brandResponseList ->
                brandResponseList.map { it.toBrand() }
            }
        )



    override suspend fun getAttributeTerms(
        attributeId: Int,
        queries: Map<String, String>
    ): Result<List<AttributeTerm>, GeneralError> =
        handleNetworkResponse(
            networkCall = { productApi.getAttributeTerms(attributeId, queries) },
            mapper = { responseList ->
                responseList.map { it.toAttributeTerm() }
            }
        )

    override suspend fun getProductReviews(
        page: Int,
        queries: Map<String, String>
    ): Result<List<Review>, GeneralError> =
        handleNetworkResponse(
            networkCall = { productApi.getProductReviews(page, queries) },
            mapper = { responseList ->
                responseList.map { it.toModel() }
            }
        )

    override suspend fun submitReview(
        review: NewReview
    ): Result<Review, GeneralError> =
        handleNetworkResponse(
            networkCall = { productApi.submitReview(review.toRequestBody()) },
            mapper = { response ->
                response.toModel()
            }
        )


//    override suspend fun getProductDetailsListById(productIds: List<Int>): Result<List<ProductDetail>, GeneralError> =
//        handleNetworkResponse(
//            networkCall = {
//                wooProductService.getProductList(
//                    queries = mapOf("per_page" to "100", "include" to productIds.joinToString(","))
//                )
//            },
//            mapper = { responseList ->
//                responseList.map { it.toProductDetail() }
//            }
//        )

    override suspend fun getCartUpdateServer(queries: Map<String, String>): Result<List<CartItemServer>, GeneralError> =

        when (val result = productApi.getCartItemUpdate(queries)) {
            is NetworkResponse.Success -> {
                val listResponse : CartCheckListResponse = result.body ?: emptyList()
                Result.Success(listResponse.map { it.toModel() })
            }

            is NetworkResponse.ApiError -> {
                val errorResponse = result.body
                Result.Failure(GeneralError.ApiError(errorResponse.error, null, null))
            }

            is NetworkResponse.NetworkError -> Result.Failure(GeneralError.NetworkError)
            is NetworkResponse.UnknownError -> Result.Failure(GeneralError.UnknownError(result.error))
        }


}