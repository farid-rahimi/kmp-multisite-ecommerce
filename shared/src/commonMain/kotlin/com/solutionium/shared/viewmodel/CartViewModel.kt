package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.CartItem
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.domain.cart.ClearCartUseCase
import com.solutionium.shared.domain.cart.ConfirmValidationUseCase
import com.solutionium.shared.domain.cart.ObserveCartUseCase
import com.solutionium.shared.domain.cart.UpdateCartItemUseCase
import com.solutionium.shared.domain.cart.ValidateCartUseCase
import com.solutionium.shared.domain.config.PaymentMethodDiscountUseCase
import com.solutionium.shared.domain.user.CheckLoginUserUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartViewModel(
    private val updateCartItemUseCase: UpdateCartItemUseCase,
    private val observeCartUseCase: ObserveCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val validateCartUseCase: ValidateCartUseCase,
    private val confirmValidation: ConfirmValidationUseCase,
    private val paymentMethodDiscountUseCase: PaymentMethodDiscountUseCase,
    private val checkLoginUserUseCase: CheckLoginUserUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(CartScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        observeCart()
        validateCart()
        loadPaymentMethodDiscounts()
    }

    fun refresh() {
        scope.launch {
            _isRefreshing.value = true
            validateCart()
            delay(600)
            _isRefreshing.value = false
        }
    }

    fun onCheckoutClick(
        onNavigateToCheckout: () -> Unit
    ) {
        scope.launch {
            val isLoggedIn = checkLoginUserUseCase().first()
            if (isLoggedIn) {
                withContext(Dispatchers.Main) {
                    onNavigateToCheckout()
                }
            } else {
                _uiState.update { it.copy(showLoginPrompt = true) }
            }
        }
    }

    fun dismissLoginPrompt() {
        _uiState.update { it.copy(showLoginPrompt = false) }
    }


    private fun loadPaymentMethodDiscounts() {
        scope.launch {
            try {
                val discounts = paymentMethodDiscountUseCase()
                _uiState.update { it.copy(paymentDiscount = discounts.values.max()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(paymentDiscount = 0.0) }
            }
        }
    }

    private fun observeCart() {
        scope.launch {
            observeCartUseCase().collect { items ->
                val totalPrice = items.sumOf { it.currentPrice * it.quantity }
                val itemsNeedAttention = items.any { it.requiresAttention }
                val cartItemCount = items.sumOf { it.quantity }

                _uiState.update { state ->
                    state.copy(
                        cartItems = items,
                        totalPrice = totalPrice,
                        cartItemCount = cartItemCount,
                        hasAttentionItems = itemsNeedAttention,
                        needsRevalidation = if (itemsNeedAttention && !state.isLoading) true else state.needsRevalidation,
                        lastValidationError = if (itemsNeedAttention) items.firstOrNull { it.validationInfo != null }?.validationInfo else null,
                    )
                }
            }
        }
    }

    fun removeItem(cartItem: CartItem) {
        scope.launch {
            updateCartItemUseCase.removeCartItem(cartItem)
        }
    }

    fun increaseQuantity(cartItem: CartItem) {
        scope.launch {
            if (cartItem.quantity >= (cartItem.currentStock ?: 12) || cartItem.quantity >= 12) return@launch // Max limit reached
            updateCartItemUseCase.increaseCartItemQuantity(cartItem.productId, cartItem.variationId)
        }
    }

    fun decreaseQuantity(cartItem: CartItem) {
        scope.launch {
            updateCartItemUseCase.decreaseCartItemQuantity(cartItem.productId, cartItem.variationId)
        }
    }

    fun validateCart() {
        scope.launch {
            _uiState.update {
                it.copy(
                    isPerformingValidation = true,
                    validationError = null,
                    validationSummaryKey = null,
                    validationSummaryCount = null,
                )
            }
            validateCartUseCase()
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val validationResults = result.data
                            val issueCount = validationResults.count { it.cartItem.requiresAttention }
                            val summaryKey = if (issueCount > 0) "cart_updated_items_title" else "all_items_updated_msg"
                            _uiState.update {
                                it.copy(
                                    isPerformingValidation = false,
                                    validationSummaryKey = summaryKey,
                                    validationSummaryCount = issueCount,
                                )
                            }
                        }
                        is Result.Failure -> {
                            val errorMessage = when(val error = result.error) {
                                is GeneralError.NetworkError -> "Network Error: Please check your connection."
                                is GeneralError.UnknownError -> "An unknown error occurred during validation."
                                else -> "Failed to validate cart."
                            }
                            _uiState.update { it.copy(isPerformingValidation = false, validationError = errorMessage) }
                        }
                    }
                }
        }
    }

    fun confirmCartValidation() {
        scope.launch {
            confirmValidation()
        }
    }

    fun clear() {
        scope.cancel()
    }
}
