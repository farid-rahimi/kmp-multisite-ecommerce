package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.Decant
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.ProductAttribute
import com.solutionium.shared.data.model.ProductVarType
import com.solutionium.shared.data.model.ProductVariation
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.model.getDecantBySize
import com.solutionium.shared.data.model.toCartItem
import com.solutionium.shared.domain.cart.AddToCartUseCase
import com.solutionium.shared.domain.cart.GetCartItemByProductUseCase
import com.solutionium.shared.domain.cart.UpdateCartItemUseCase
import com.solutionium.shared.domain.config.PaymentMethodDiscountUseCase
import com.solutionium.shared.domain.favorite.ObserveFavoritesUseCase
import com.solutionium.shared.domain.favorite.ToggleFavoriteUseCase
import com.solutionium.shared.domain.review.GetTopReviewsUseCase
import com.solutionium.shared.domain.products.GetProductDetailsUseCase
import com.solutionium.shared.domain.products.GetProductVariationsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    initialArgs: Map<String, String> = emptyMap(),
    private val getProductDetails: GetProductDetailsUseCase,
    private val getProductVariations: GetProductVariationsUseCase,
    private val addToCart: AddToCartUseCase,
    private val updateCartItemUseCase: UpdateCartItemUseCase,
    private val getProductInCartQuantity: GetCartItemByProductUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val paymentMethodDiscountUseCase: PaymentMethodDiscountUseCase,
    private val getTopReviews: GetTopReviewsUseCase,

    ) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // --- State Holder Instance ---
    // The ViewModel owns the state holder and provides its scope.
    private val variationStateHolder = VariationSelectionStateHolder(scope)

    // --- Expose State to the UI ---
    // The UI will observe these flows directly from the ViewModel.
    val selectedOptions: StateFlow<Map<Int, String>> = variationStateHolder.selectedOptions
    val variations: StateFlow<List<ProductVariation>> = variationStateHolder.variations
    val selectedVariation: StateFlow<ProductVariation?> = variationStateHolder.selectedVariation
    val selectedImageUrl: StateFlow<String?> = variationStateHolder.selectedImageUrl
    val productVariationAttributes: StateFlow<List<ProductAttribute>> =
        variationStateHolder.variationAttributes


    private val _state: MutableStateFlow<ProductDetailState> =
        MutableStateFlow(ProductDetailState())
    val state = _state.asStateFlow()

    // --- State and Event Handlers ---
    // Use a Decant object for selection state, or null for "Full Bottle"
    private val _selectedDecant = MutableStateFlow<Decant?>(null)
    val selectedDecant: StateFlow<Decant?> = _selectedDecant.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()


//    val currentPrice: StateFlow<Double> = combine(
//        state.map { it.product?.price }.distinctUntilChanged(),
//        selectedDecant
//    ) { fullBottlePrice, decant ->
//        decant?.price ?: fullBottlePrice ?: 0.0
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun onDecantSelected(decant: Decant) {
        // If the user selects the same decant again, deselect it to default to full bottle
        _selectedDecant.value = decant //if (_selectedDecant.value == decant) null else
    }

    fun onFullBottleSelected() {
        _selectedDecant.value = null // Set to null to signify "Full Bottle"
    }

    private val productId: Int = initialArgs["productId"]?.toIntOrNull() ?: -1
    private val productSlug: String? = initialArgs["productSlug"]

    init {
        fetchData()
    }

    private fun fetchData() {
        load()
        observeCartQuantity()
        observeFavorites()
        loadPaymentMethodDiscounts()
        loadTopReviews()
    }

    fun refresh() {
        scope.launch {
            _isRefreshing.value = true
            // Re-run all the initial data fetching logic
            fetchData()
            // A small delay can make the UI feel smoother
            delay(600)
            _isRefreshing.value = false
        }
    }

    private fun loadTopReviews() {
        scope.launch {
            getTopReviews(productId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val reviews = result.data
                        _state.update { it.copy(comments = reviews) }
                    }

                    is Result.Failure -> {
                        // Handle error if needed
                    }
                }
            }

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

    private fun observeCartQuantity() {
        scope.launch {
            // This pipeline now reacts to changes in `selectedVariation` automatically.
            combine(selectedVariation, selectedDecant) { variation, decant ->
                Pair(variation, decant)
            }.flatMapLatest { (variation, decant) ->
                when {
                    // Case 1: Variable product selected
                    state.value.isVariable && variation != null -> {
                        getProductInCartQuantity(productId, variation.id)
                    }
                    // Case 2: Simple product with a decant selected
                    !state.value.isVariable && decant != null -> {
                        val decantId = decant.size.hashCode() // Use size hashcode as the pseudo-ID
                        getProductInCartQuantity(productId, decantId)
                    }
                    // Case 3: Simple product, "Full Bottle" selected (no decant)
                    !state.value.isVariable && decant == null -> {
                        getProductInCartQuantity(productId, 0) // '0' represents the main product
                    }

                    else -> flowOf(null)
                }
            }.collect { item ->
                // This single collector updates the state for all cases.
                _state.update { it.copy(cartItem = item) }
            }
        }
    }

    private fun load() = scope.launch {
        _state.update { it.copy(isLoading = true) }

        val result = if (productId > 0) getProductDetails(productId) else getProductDetails(
            productSlug ?: ""
        )

        when (result) {
            is Result.Success -> {

                _state.update {
                    it.copy(
                        product = result.data,
                        isVariable = result.data.varType == ProductVarType.VARIABLE,
                        isLoading = false,
                        error = null
                    )
                }
                if (result.data.varType == ProductVarType.VARIABLE) {
                    loadProductVariations(productId)
                }
                if (result.data.stockStatus == "onbackorder") {
                    _selectedDecant.value = result.data.decants.first()
                }
                // Start observing cart quantity after loading product details
                //observeCartQuantity()

            }

            is Result.Failure -> {
                when (result.error) {
                    is GeneralError.ApiError -> {
                        _state.update {
                            it.copy(
                                error = result.error,
                                message = (result.error as GeneralError.ApiError).message,
                                isLoading = false,
                            )
                        }

                    }

                    GeneralError.NetworkError -> {
                        _state.update {
                            it.copy(
                                error = result.error,
                                message = "No internet connection. Please check your network settings.",
                                isLoading = false,
                            )
                        }

                    }

                    is GeneralError.UnknownError ->
                        _state.update {
                            it.copy(
                                error = result.error,
                                message = (result.error as GeneralError.UnknownError).error.message,
                                //isLoading = false,
                            )
                        }


                }
            }
        }
    }

    // --- Data Fetching ---
    private fun loadProductVariations(productId: Int) {
        scope.launch {
            _state.value = _state.value.copy(isLoadingVariations = true)


            when (val result = getProductVariations(productId)) {
                is Result.Success -> {
                    // 4. POWER UP THE STATE HOLDER with the fetched data
                    variationStateHolder.setProductData(
                        attributes = _state.value.product?.attributes?.filter { it.variation }
                            ?: emptyList(),
                        variations = result.data,
                        defaultAttributes = _state.value.product?.defaultAttributes
                            ?: emptyList(), // Pass defaults for pre-selection
                    )
                    _state.value = _state.value.copy(isLoadingVariations = false, error = null)
                }

                is Result.Failure -> {

                    _state.update {
                        it.copy(
                            error = result.error,
                            message = (result.error as GeneralError.ApiError).message,
                            isLoadingVariations = false,
                        )
                    }

                }
            }

        }
    }

    fun onOptionSelected(attributeId: Int, optionValue: String) {
        variationStateHolder.onOptionSelected(attributeId, optionValue)
    }

    /**
     * The UI calls this to check if an option should be grayed out.
     */
    fun isOptionAvailable(attributeId: Int, optionValue: String): Boolean {
        //return true
        return variationStateHolder.isOptionAvailable(attributeId, optionValue)
    }

    fun onAddToCartClick() {
//        viewModelScope.launch {
//            _state.value.product?.let { product ->
//                if (state.value.isVariable) {
//                    val variation = selectedVariation.value
//                    if (variation == null) {
//                        _state.update { it.copy(message = "Please select all options") }
//                        return@launch
//                    } else {
//                        // Use the NEW toCartItem extension for variations
//                        addToCart(product.toCartItem(variation))
//                    }
//                } else {
//                    // Use the original toCartItem for simple products
//                    addToCart(product.toCartItem())
//                }
//            }
//        }

        scope.launch {
            _state.value.product?.let { product ->
                val decantSelection = selectedDecant.value

                when {
                    // Case 1: Variable Product
                    state.value.isVariable -> {
                        val variation = selectedVariation.value
                        if (variation == null) {
                            _state.update { it.copy(message = "Please select all options") }
                            return@launch
                        } else {
                            // Use the NEW toCartItem extension for variations
                            addToCart(product.toCartItem(variation))
                        }
                    }

                    // Case 2: Simple Product with a Decant selected
                    decantSelection != null -> {
                        val decantItem = product.toCartItem().copy(
                            variationId = decantSelection.size.hashCode(), //${if (cartItem.isDecant) "دکانت" else ""}
                            name = "دکانت ${decantSelection.size} ${product.name}", // "Perfume (5ml)"
                            decVol = getDecantBySize(decantSelection.size)?.toInt().toString(),
                            currentPrice = decantSelection.price, // Use the specific decant price
                            regularPrice = if (product.onSale) decantSelection.regularPrice else null,
                            isDecant = true,
                            shippingClass = ""
                        )
                        addToCart(decantItem)
                    }

                    // Case 3: Normal Simple Product OR "Full Bottle" is selected
                    else -> {
                        // Use '0' for variationId to represent the main product/full bottle
                        val fullBottleItem = product.toCartItem()
                        addToCart(fullBottleItem)
                    }
                }
            }
        }
    }

    fun removeItem() {
        scope.launch {
            state.value.cartItem?.let {
                if (it.quantity > 1) {
                    // Pass both IDs to uniquely identify the item to update
                    updateCartItemUseCase.decreaseCartItemQuantity(it.productId, it.variationId)
                } else {
                    updateCartItemUseCase.removeCartItem(it)
                }
            }
        }
    }

    fun increaseQuantity() {
        scope.launch {
            state.value.cartItem?.let {
                // Pass both IDs to uniquely identify the item to update
                updateCartItemUseCase.increaseCartItemQuantity(it.productId, it.variationId)
            }
        }
    }

    fun toggleFavorite() {
        scope.launch {
            toggleFavoriteUseCase(productId, state.value.isFavorite())
        }
    }

    fun onRetry() {
        load()
    }

    fun clear() {
        scope.cancel()
    }
}
