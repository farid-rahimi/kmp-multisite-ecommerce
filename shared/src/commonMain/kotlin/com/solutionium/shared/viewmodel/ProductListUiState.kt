package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.CartItem

data class ProductListUiState(
    val isLoading: Boolean,
    val title: String? = null,
    val cartItems: List<CartItem> = emptyList(),
    val favoriteIds: List<Int> = emptyList(),
    val paymentDiscount: Double? = null,
    val isSuperUser: Boolean = false
) {
    fun cartItemCount(productId: Int): Int =
        cartItems.find { it.productId == productId && it.variationId == 0}?.quantity ?: 0

    fun isFavorite(productId: Int): Boolean =
        favoriteIds.contains(productId)


    fun discountedPrice(originalPrice: Double?): Double? =
        originalPrice?.let { paymentDiscount?.let { (100 - it) / 100 }?.let { it * originalPrice }}

}