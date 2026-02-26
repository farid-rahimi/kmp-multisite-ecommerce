package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.AttributeTerm
import com.solutionium.shared.data.model.AttributeTermsListType
import com.solutionium.shared.data.model.Brand
import com.solutionium.shared.data.model.BrandListType
import com.solutionium.shared.data.model.ProductThumbnail
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.domain.config.GetAppImages
import com.solutionium.shared.domain.products.GetAttributeTermsUseCase
import com.solutionium.shared.domain.products.GetBrandsUseCase
import com.solutionium.shared.domain.products.SearchProductsUseCase
import com.solutionium.shared.domain.user.CheckSuperUserUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CategoryDisplayType {
    MAIN,
    ALL_BRANDS,
    ALL_SCENT_GROUPS,
}

data class CategoryScreenState(
    val images: Map<Int, String> = emptyMap(),
    val topScentGroups: List<AttributeTerm> = emptyList(),
    val perfumeSpotlightTerms: List<AttributeTerm> = emptyList(),
    val perfumeSeasonTerms: List<AttributeTerm> = emptyList(),
    val perfumeBrands: List<Brand> = emptyList(),
    val showShoes: Boolean = false,
    val shoeBrands: List<Brand> = emptyList(),
    val allBrands: List<Brand> = emptyList(),
    val allScentGroups: List<AttributeTerm> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val categoryDisplayType: CategoryDisplayType = CategoryDisplayType.MAIN,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<ProductThumbnail> = emptyList(),
    val isSuperUser: Boolean = false,
)

class CategoryViewModel(
    private val getBrands: GetBrandsUseCase,
    private val getAttributeTerms: GetAttributeTermsUseCase,
    private val getAppImages: GetAppImages,
    private val searchProducts: SearchProductsUseCase,
    private val checkSuperUserUserCase: CheckSuperUserUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(CategoryScreenState())
    val uiState = _uiState.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchData()
    }

    private fun fetchData() {
        checkSuperUser()
        loadImages()
        observeSearchQuery()

        loadAttributeTerms(AttributeTermsListType.Genders) { terms ->
            _uiState.update { it.copy(perfumeSpotlightTerms = terms) }
        }
        loadBrands(BrandListType.TopPerfumes, sortByMenuOrder = true) { brands ->
            _uiState.update { it.copy(perfumeBrands = brands) }
        }
        loadAttributeTerms(AttributeTermsListType.TopScentGroup) { terms ->
            _uiState.update { it.copy(topScentGroups = terms) }
        }
        loadAttributeTerms(AttributeTermsListType.Seasons) { terms ->
            _uiState.update { it.copy(perfumeSeasonTerms = terms) }
        }
        loadBrands(BrandListType.TopShoes) { brands ->
            _uiState.update { it.copy(shoeBrands = brands) }
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

    private fun checkSuperUser() {
        scope.launch {
            val isLoggedIn = checkSuperUserUserCase().first()
            _uiState.update { it.copy(isSuperUser = isLoggedIn) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        searchJob?.cancel()
        searchJob = scope.launch {
            searchQueryFlow
                .debounce(300L)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    searchProducts(query)
                        .onStart { _uiState.update { it.copy(isSearching = true) } }
                        .onCompletion { _uiState.update { it.copy(isSearching = false) } }
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update { it.copy(searchResults = result.data, isSearching = false) }
                        }

                        is Result.Failure -> {
                            _uiState.update { it.copy(error = result.error.toString(), isSearching = false) }
                        }
                    }
                }
        }
    }

    fun clearSearch() {
        onSearchQueryChanged("")
        _uiState.update { it.copy(searchResults = emptyList()) }
    }

    private fun loadImages() {
        scope.launch {
            val result = getAppImages()
            _uiState.update { it.copy(images = result) }
        }
    }

    private fun loadBrands(
        listType: BrandListType,
        sortByMenuOrder: Boolean = false,
        onResult: (List<Brand>) -> Unit,
    ) {
        scope.launch {
            getBrands(listType)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                .onEach { result ->
                    when (result) {
                        is Result.Success -> {
                            onResult(result.data.sortedBy { if (sortByMenuOrder) it.menuOrder else 0 })
                        }

                        is Result.Failure -> Unit
                    }
                }
                .collect()
        }
    }

    private fun loadAttributeTerms(
        type: AttributeTermsListType,
        onResult: (List<AttributeTerm>) -> Unit,
    ) {
        scope.launch {
            getAttributeTerms(type)
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                .onEach { result ->
                    when (result) {
                        is Result.Success -> onResult(result.data.sortedBy { it.menuOrder })
                        is Result.Failure -> Unit
                    }
                }
                .collect()
        }
    }

    fun backToMainDisplay() {
        _uiState.update { it.copy(categoryDisplayType = CategoryDisplayType.MAIN) }
    }

    fun goToAllBrands() {
        loadBrands(BrandListType.All) { brands ->
            _uiState.update { it.copy(allBrands = brands) }
        }
        _uiState.update { it.copy(categoryDisplayType = CategoryDisplayType.ALL_BRANDS) }
    }

    fun goToAllScentGroups() {
        loadAttributeTerms(AttributeTermsListType.AllScentGroup) { terms ->
            _uiState.update { it.copy(allScentGroups = terms) }
        }
        _uiState.update { it.copy(categoryDisplayType = CategoryDisplayType.ALL_SCENT_GROUPS) }
    }

    fun clear() {
        searchJob?.cancel()
        scope.cancel()
    }
}
