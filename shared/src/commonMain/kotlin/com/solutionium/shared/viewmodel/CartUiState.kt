package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.CartItem
import com.solutionium.shared.data.model.ValidationInfo
data class CartScreenUiState(
    val cartItems: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val cartItemCount: Int = 0,
    val isLoading: Boolean = false,
    val lastValidationError: ValidationInfo? = null,
    val needsRevalidation: Boolean = false, // Flag if background validation found changes
    val checkoutInProgress: Boolean = false,
    val isPerformingValidation: Boolean = false, // Specific state for the validation process
    val validationError: String? = null,
    val hasAttentionItems: Boolean = false,
    val validationSummaryKey: String? = null,
    val validationSummaryCount: Int? = null,
    val paymentDiscount: Double = 0.0,
    val installmentPriceEnabled: Boolean = false,

    val isUserLoggedIn: Boolean = true, // Assume true by default
    val showLoginPrompt: Boolean = false // Controls the dialog


) {

    fun discountedPrice(originalPrice: Double?): Double? =
        originalPrice?.let { paymentDiscount.let { (100 - it) / 100 } * originalPrice }

}
