package com.solutionium.shared.viewmodel

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.solutionium.shared.data.model.FilterCriterion
import com.solutionium.shared.data.model.PRODUCT_ARG_TITLE
import com.solutionium.shared.data.model.ProductThumbnail
import com.solutionium.shared.data.model.toCartItem
import com.solutionium.shared.domain.cart.AddToCartUseCase
import com.solutionium.shared.domain.cart.ObserveCartUseCase
import com.solutionium.shared.domain.cart.UpdateCartItemUseCase
import com.solutionium.shared.domain.config.PaymentMethodDiscountUseCase
import com.solutionium.shared.domain.favorite.ObserveFavoritesUseCase
import com.solutionium.shared.domain.favorite.ToggleFavoriteUseCase
import com.solutionium.shared.domain.user.CheckSuperUserUseCase
import com.solutionium.shared.domain.products.GetProductListStreamUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductListViewModel(
    initialArgs: Map<String, String> = emptyMap(),
    private val productList: GetProductListStreamUseCase,
    private val observeCartUseCase: ObserveCartUseCase,
    private val addToCart: AddToCartUseCase,
    private val updateCartItem: UpdateCartItemUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val paymentMethodDiscountUseCase: PaymentMethodDiscountUseCase,
    private val checkSuperUserUserCase: CheckSuperUserUseCase
    //private val checkLoginUserUseCase: CheckLoginUserUseCase,


    ) {


    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val filters: MutableList<FilterCriterion> =
        ProductListFilters().buildFilterCriteria(initialArgs)
    //private val filterFlow = MutableStateFlow(ProductListFilters().buildFilterCriteria(savedStateHandle))


    private val _state = MutableStateFlow(ProductListUiState(isLoading = false))
    val state = _state.asStateFlow()


    init {

        val screenTitle: String? = initialArgs[PRODUCT_ARG_TITLE]
        _state.update { it.copy(title = screenTitle) }

        observeCart()
        observeFavorites()
        loadPaymentMethodDiscounts()

        checkSuperUser()
    }



    private fun checkSuperUser() {
        scope.launch {
            val isLoggedIn = checkSuperUserUserCase().first()
            _state.update { it.copy(isSuperUser = isLoggedIn) }
        }
    }


    private fun loadPaymentMethodDiscounts() {
        scope.launch {
            val discounts = paymentMethodDiscountUseCase()
            _state.update { it.copy(paymentDiscount = discounts.values.maxOrNull()) }
        }
    }


    private fun observeFavorites() {
        scope.launch {
            observeFavoritesUseCase().collect { favoriteIds ->

                _state.update { it.copy(favoriteIds = favoriteIds) }

            }
        }
    }

    private fun observeCart() {
        scope.launch {
            observeCartUseCase().collect { cartItems ->
                _state.update { it.copy(cartItems = cartItems) }
            }
        }
    }

    val pagedList: Flow<PagingData<ProductThumbnail>> =
        //filterFlow.flatMapLatest { currentFilters ->
            productList(filters)
                .map { pagingData ->
                    val items = mutableListOf<ProductThumbnail>()
                    pagingData.filter { productThumbnail ->
                        items.contains(productThumbnail)
                            .not()
                            .also { shouldAdd ->
                                if (shouldAdd) {
                                    items.add(productThumbnail)
                                }
                            }
                    }
                //}
        }.cachedIn(scope)

    fun addToCart(product: ProductThumbnail) {
        scope.launch {
            val existingItemCount = state.value.cartItemCount(product.id)
            if (existingItemCount > 0) {
                updateCartItem.increaseCartItemQuantity(product.id)
            } else
                addToCart(product.toCartItem())
        }
    }

    fun removeFromCart(productId: Int) {
        scope.launch {
            updateCartItem.decreaseCartItemQuantity(productId)
        }
    }

    fun toggleFavorite(productId: Int, isCurrentlyFavorite: Boolean) {
        scope.launch {
//            if (checkLoginUserUseCase().first()) {
//                // User is logged in, proceed as normal
//                toggleFavoriteUseCase(productId, isCurrentlyFavorite)
//            } else {
//                // User is not logged in, update the state to show the prompt
//                GlobalUiState.showLoginPrompt()
//            }
            toggleFavoriteUseCase(productId, isCurrentlyFavorite)
        }
    }

    fun clear() {
        scope.cancel()
    }
}
