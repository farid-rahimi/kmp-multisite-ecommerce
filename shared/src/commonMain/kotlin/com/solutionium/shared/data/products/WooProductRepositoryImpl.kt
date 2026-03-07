package com.solutionium.shared.data.products

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.solutionium.shared.data.api.woo.WooProductsRemoteSource
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

internal class WooProductRepositoryImpl(
    private val wooProductsRemoteSource: WooProductsRemoteSource,
) : WooProductRepository {


    override suspend fun getProductDetails(productId: Int): Result<ProductDetail, GeneralError> {

        return wooProductsRemoteSource.getProductDetails(productId)
//        GlobalScope.launch {
//            when(val result = wooProductsRemoteSource.getProductDetails(productId)) {
//                is Result.Success -> productDetailDao.insertProductDetail(result.data.toEntity())
//                is Result.Failure -> flow {
//                    emit(Result.Failure(result.error))
//                }
//            }
//        }

//        return productDetailDao.getProductDetails(productId)
//            .catch { Result.Success(it) }
//            .map {
//                if (it != null) {
//                    Result.Success(it.toModel())
//                } else {
//                    Result.Failure(GeneralError.UnknownError(Throwable("Data not found")))
//                }
//            }
    }

    override suspend fun getProductDetails(slug: String): Result<ProductDetail, GeneralError> {

        return wooProductsRemoteSource.getProductDetails(slug)
    }



    override suspend fun getProductsList(
        queries: Map<String, String>,
    ): Result<List<ProductThumbnail>, GeneralError> =
        wooProductsRemoteSource.getProductList(1, queries)

    override suspend fun getProductVariations(productId: Int): Result<List<ProductVariation>, GeneralError> =
        wooProductsRemoteSource.getProductVariations(productId)

    override suspend fun getCartUpdateServer(
        productIds: List<Int>,
    ): Result<List<CartItemServer>, GeneralError> =
        wooProductsRemoteSource.getCartUpdateServer(
            mapOf("include" to productIds.joinToString(","))
        )


    override fun getProductListStream(
        queries: Map<String, String>,
    ): Flow<PagingData<ProductThumbnail>> =
        Pager(
            config = PagingConfig(
                pageSize = WooProductsRemoteSource.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                ProductsPagingSource2(
                    queries = queries,
                    wooProductsRemoteSource = wooProductsRemoteSource,
                )
            },
        ).flow


    override suspend fun getReviewList(
        queries: Map<String, String>,
    ): Result<List<Review>, GeneralError> =
        wooProductsRemoteSource.getProductReviews(1, queries)

    override fun getReviewListStream(
        queries: Map<String, String>,
    ): Flow<PagingData<Review>> =
        Pager(
            config = PagingConfig(
                pageSize = WooProductsRemoteSource.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                ReviewsPagingSource(
                    queries = queries,
                    wooProductsRemoteSource = wooProductsRemoteSource,
                )
            },
        ).flow

    override suspend fun submitReview(review: NewReview): Result<Review, GeneralError> =
        wooProductsRemoteSource.submitReview(review)

    override suspend fun getBrandList(type: BrandListType): Result<List<Brand>, GeneralError> =
        wooProductsRemoteSource.getBrandList(type.queries)

    override suspend fun getBrandList(
        queries: Map<String, String>,
    ): Result<List<Brand>, GeneralError> =
        wooProductsRemoteSource.getBrandList(queries)

    override suspend fun getAttributeTerms(listType: AttributeTermsListType): Result<List<AttributeTerm>, GeneralError> =
        wooProductsRemoteSource.getAttributeTerms(attributeId = listType.attributeId, queries = listType.queries)

    override suspend fun getAttributeTerms(
        attributeId: Int,
        queries: Map<String, String>,
    ): Result<List<AttributeTerm>, GeneralError> =
        wooProductsRemoteSource.getAttributeTerms(attributeId = attributeId, queries = queries)

//    override suspend fun getProductDetailsForValidation(productIds: List<Int>): Result<List<ProductDetail>, GeneralError> =
//        wooProductsRemoteSource.getProductDetailsListById(productIds)

}
