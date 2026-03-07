package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.Link
import com.solutionium.shared.data.model.ProductListType
import com.solutionium.shared.data.model.ProductThumbnail
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.model.toCartItem
import com.solutionium.shared.domain.cart.AddToCartUseCase
import com.solutionium.shared.domain.cart.ObserveCartUseCase
import com.solutionium.shared.domain.cart.UpdateCartItemUseCase
import com.solutionium.shared.domain.categories.GetCategoryListUseCase
import com.solutionium.shared.domain.config.GetContactInfoUseCase
import com.solutionium.shared.domain.config.GetHeaderLogoUseCase
import com.solutionium.shared.domain.config.GetStoriesUseCase
import com.solutionium.shared.domain.config.GetVersionsUseCase
import com.solutionium.shared.domain.config.HomeBannersUseCase
import com.solutionium.shared.domain.config.PaymentMethodDiscountUseCase
import com.solutionium.shared.domain.favorite.ObserveFavoritesUseCase
import com.solutionium.shared.domain.favorite.ToggleFavoriteUseCase
import com.solutionium.shared.domain.products.GetProductsListUseCase
import com.solutionium.shared.domain.user.AddStoryViewUseCase
import com.solutionium.shared.domain.user.CheckLoginUserUseCase
import com.solutionium.shared.domain.user.CheckSuperUserUseCase
import com.solutionium.shared.domain.user.GetAllStoryViewUseCase
import com.solutionium.shared.domain.user.ObserveLanguageUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

fun interface AppVersionProvider {
    fun getCurrentVersionName(): String?
}

sealed interface HomeNavigationEvent {
    data class ToProduct(val productId: Int) : HomeNavigationEvent
    data class ToProductList(val params: Map<String, String>) : HomeNavigationEvent
    data class ToExternalLink(val url: String) : HomeNavigationEvent
}

class HomeViewModel(
    private val getProductsUseCase: GetProductsListUseCase,
    private val getCategoriesUseCase: GetCategoryListUseCase,
    private val observeCartUseCase: ObserveCartUseCase,
    private val addToCart: AddToCartUseCase,
    private val updateCartItem: UpdateCartItemUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val homeBannerUseCase: HomeBannersUseCase,
    private val paymentMethodDiscountUseCase: PaymentMethodDiscountUseCase,
    private val getStoriesUseCase: GetStoriesUseCase,
    private val addStoryViewUseCase: AddStoryViewUseCase,
    private val getAllViewedStories: GetAllStoryViewUseCase,
    private val getHeaderLogoUseCase: GetHeaderLogoUseCase,
    private val checkLoginUserUseCase: CheckLoginUserUseCase,
    private val checkSuperUserUseCase: CheckSuperUserUseCase,
    private val getVersionsUseCase: GetVersionsUseCase,
    private val getContactInfoUseCase: GetContactInfoUseCase,
    private val appVersionProvider: AppVersionProvider,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(HomeUiState(isLoading = false))
    val state = _state.asStateFlow()

    private val _bannerState = MutableStateFlow(BannerSliderState(isLoading = false))
    val bannerState = _bannerState.asStateFlow()

    private val sessionViewedStories = MutableStateFlow<Set<Int>>(emptySet())

    private val _navigationEvent = MutableSharedFlow<HomeNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchData()
        observeLanguageChanges()
    }

    private fun observeLanguageChanges() {
        scope.launch {
            var isFirstEmission = true
            observeLanguageUseCase().collect {
                if (isFirstEmission) {
                    isFirstEmission = false
                    return@collect
                }
                loadStories()
                loadBanners()
                loadContactInfo()
            }
        }
    }

    private fun fetchData() {
        scope.launch {
            val headerUrl = getHeaderLogoUseCase()
            _state.update { it.copy(headerLogoUrl = headerUrl) }
        }

        checkSuperUser()
        loadStories()
        observeCart()

        scope.launch {
            _state.update { it.copy(newArrivalsLoading = true) }
            loadProducts(ProductListType.New) { result ->
                _state.update { it.copy(newArrivals = result, newArrivalsLoading = false) }
            }

            _state.update { it.copy(appOffersLoading = true) }
            loadProducts(ProductListType.Offers) { result ->
                _state.update { it.copy(appOffers = result, appOffersLoading = false) }
            }

            _state.update { it.copy(featuredLoading = true) }
            loadProducts(ProductListType.Features) { result ->
                _state.update { it.copy(featured = result, featuredLoading = false) }
            }

            _state.update { it.copy(onSalesLoading = true) }
            loadProducts(ProductListType.OnSale) { result ->
                _state.update { it.copy(onSales = result, onSalesLoading = false) }
            }
        }

        loadBanners()
        observeFavorites()
        loadPaymentMethodDiscounts()
        observeSession()
        checkAppVersion()
        loadContactInfo()
    }

    private fun checkAppVersion() {
        scope.launch {
            try {
                val config = getVersionsUseCase() ?: return@launch
                val latestVersionName = config.latestVersion
                val minimumVersionName = config.minRequiredVersion
                val currentVersionName = appVersionProvider.getCurrentVersionName() ?: return@launch

                val updateType = when {
                    currentVersionName < minimumVersionName -> UpdateType.FORCED
                    currentVersionName < latestVersionName -> UpdateType.RECOMMENDED
                    else -> UpdateType.NONE
                }

                if (updateType != UpdateType.NONE) {
                    _state.update {
                        it.copy(
                            updateInfo = UpdateInfo(
                                type = updateType,
                                latestVersionName = latestVersionName,
                            ),
                        )
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun loadContactInfo() {
        scope.launch {
            val contactInfo = getContactInfoUseCase()
            _state.update { it.copy(contactInfo = contactInfo) }
        }
    }

    fun refresh() {
        scope.launch {
            _isRefreshing.value = true
            fetchData()
            delay(600)
            _isRefreshing.value = false
        }
    }

    fun dismissUpdateDialog() {
        _state.update { it.copy(updateInfo = it.updateInfo.copy(type = UpdateType.NONE)) }
    }

    fun showContactSupport() {
        _state.update { it.copy(showContactSupportDialog = true) }
    }

    fun dismissContactSupport() {
        _state.update { it.copy(showContactSupportDialog = false) }
    }

    private fun observeSession() {
        scope.launch {
            checkLoginUserUseCase().collect { result ->
                _state.update { it.copy(isLogin = result) }
            }
        }
    }

    private fun checkSuperUser() {
        scope.launch {
            val isSuperUser = checkSuperUserUseCase().first()
            _state.update { it.copy(isSuperUser = isSuperUser) }
        }
    }

    private fun loadStories() {
        scope.launch {
            _state.update { it.copy(storiesLoading = true) }
            val serverStories = getStoriesUseCase()
            if (serverStories.isNotEmpty()) {
                _state.update { it.copy(serverStoryItems = serverStories, storiesLoading = false) }
                observeViewedStories()
            }
        }
    }

    private fun observeViewedStories() {
        scope.launch {
            getAllViewedStories().collect { viewedItems ->
                val viewedIds = viewedItems.map { it.storyId }.toSet()
                val originalStories = _state.value.serverStoryItems

                val storiesWithUpdatedFlag = originalStories.map { story ->
                    story.copy(isViewed = story.id in viewedIds)
                }
                val (unread, read) = storiesWithUpdatedFlag.partition { !it.isViewed }
                val viewedTimeMap = viewedItems.associateBy({ it.storyId }, { it.viewedAt })
                val sortedRead = read.sortedByDescending { viewedTimeMap[it.id] }

                _state.update { state ->
                    state.copy(storyItems = (unread + sortedRead))
                }
            }
        }
    }

    fun setStoryAsViewedInSession(storyId: Int) {
        sessionViewedStories.update { it + storyId }
    }

    fun persistViewedStories() {
        scope.launch {
            sessionViewedStories.value.forEach { id ->
                addStoryViewUseCase(id)
            }
            sessionViewedStories.value = emptySet()
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
                _state.update { currentState ->
                    currentState.copy(cartItems = cartItems)
                }
            }
        }
    }

    private fun loadBanners() {
        scope.launch {
            _bannerState.value = _bannerState.value.copy(isLoading = true)
            val banners = homeBannerUseCase()
            _bannerState.value = _bannerState.value.copy(banners = banners, isLoading = false)
        }
    }

    private suspend fun loadCategories() {
        getCategoriesUseCase().onStart { _state.update { state -> state.copy(isLoading = true) } }
            .onCompletion { _state.update { state -> state.copy(isLoading = false) } }
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update { state ->
                            state.copy()
                        }
                    }

                    is Result.Failure -> Unit
                }
            }.collect()
    }

    private suspend fun loadProducts(
        listType: ProductListType,
        onResult: (List<ProductThumbnail>) -> Unit,
    ) {
        getProductsUseCase(listType).collect { result ->
            when (result) {
                is Result.Success -> onResult(result.data)
                is Result.Failure -> onResult(emptyList())
            }
        }
    }

    fun addToCart(product: ProductThumbnail) {
        scope.launch {
            val existingItemCount = state.value.cartItemCount(product.id)
            if (existingItemCount > 0) {
                updateCartItem.increaseCartItemQuantity(product.id)
            } else {
                addToCart(product.toCartItem())
            }
        }
    }

    fun removeFromCart(productId: Int) {
        scope.launch {
            updateCartItem.decreaseCartItemQuantity(productId)
        }
    }

    fun toggleFavorite(productId: Int, isCurrentlyFavorite: Boolean) {
        scope.launch {
            toggleFavoriteUseCase(productId, isCurrentlyFavorite)
        }
    }

    fun onLinkClick(link: Link) {
        scope.launch {
            when {
                link.isProductLink -> {
                    val productId = link.target.toIntOrNull() ?: return@launch
                    _navigationEvent.emit(HomeNavigationEvent.ToProduct(productId))
                }

                link.isProductsLink -> {
                    _navigationEvent.emit(HomeNavigationEvent.ToProductList(link.getRouteQuery()))
                }

                link.isExternalLink -> {
                    _navigationEvent.emit(HomeNavigationEvent.ToExternalLink(link.target))
                }
            }
        }
    }

    fun clear() {
        scope.cancel()
    }
}
