package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.CartItem
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.ProductDetail
import com.solutionium.shared.data.model.Review

data class ProductDetailState(
    val product: ProductDetail? = null,
    val cartItem: CartItem? = null,
    val comments: List<Review> = emptyList(),
    val averageRating: Float? = null,
    val isLoading: Boolean = false,
    val isLoadingVariations: Boolean = false,
    val isVariable: Boolean = false,
    val message: String? = null,
    val error: GeneralError? = null,
    val favoriteIds: List<Int> = emptyList(),
    val paymentDiscount: Double? = null
) {
    fun isFavorite(): Boolean =
         product?.id?.let { favoriteIds.contains(it) } ?: false

    fun discountedPrice(originalPrice: Double?): Double? =
        originalPrice?.let { paymentDiscount?.let { (100 - it) / 100 }?.let { it * originalPrice }}

}

