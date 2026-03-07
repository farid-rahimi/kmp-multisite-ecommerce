package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.AttributeTerm
import com.solutionium.shared.data.model.AttributeTermsListType
import com.solutionium.shared.data.model.Brand
import com.solutionium.shared.data.model.BrandListType
import com.solutionium.shared.data.model.DisplayableTerm
import com.solutionium.shared.data.model.Link
import com.solutionium.shared.data.model.LinkType
import com.solutionium.shared.data.model.PRODUCT_ARG_ATTRIBUTE
import com.solutionium.shared.data.model.PRODUCT_ARG_ATTRIBUTE_TERM
import com.solutionium.shared.data.model.PRODUCT_ARG_BRAND_ID
import com.solutionium.shared.data.model.PRODUCT_ARG_TITLE
import com.solutionium.shared.data.model.ProductThumbnail
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.model.SearchTabConfig
import com.solutionium.shared.data.model.SearchTabViewType
import com.solutionium.shared.domain.config.GetAppImages
import com.solutionium.shared.domain.config.GetSearchTabsUseCase
import com.solutionium.shared.domain.products.GetAttributeTermsUseCase
import com.solutionium.shared.domain.products.GetBrandsUseCase
import com.solutionium.shared.domain.products.SearchProductsUseCase
import com.solutionium.shared.domain.user.CheckSuperUserUseCase
import com.solutionium.shared.domain.user.ObserveLanguageUseCase
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
    ALL_ITEMS,
}

enum class CategoryAllItemsKind {
    BRAND,
    ATTRIBUTE,
}

data class CategoryDynamicSection(
    val id: Int,
    val title: String,
    val type: String,
    val source: String,
    val sourceSlug: String?,
    val viewType: SearchTabViewType,
    val items: List<DisplayableTerm>,
    val moreTitle: String?,
    val moreLink: Link?,
)

data class CategoryAllItemsState(
    val kind: CategoryAllItemsKind,
    val title: String,
    val items: List<DisplayableTerm>,
    val attributeSource: String? = null,
    val attributeFilterKey: String? = null,
)

data class CategoryScreenState(
    val images: Map<Int, String> = emptyMap(),
    val dynamicSections: List<CategoryDynamicSection> = emptyList(),
    val allItemsState: CategoryAllItemsState? = null,
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
    private val getSearchTabs: GetSearchTabsUseCase,
    private val searchProducts: SearchProductsUseCase,
    private val checkSuperUserUserCase: CheckSuperUserUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
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
        observeLanguageChanges()
    }

    private fun fetchData() {
        checkSuperUser()
        loadImages()
        observeSearchQuery()
        loadDynamicSections()
    }

    private fun observeLanguageChanges() {
        scope.launch {
            var isFirstEmission = true
            observeLanguageUseCase().collect {
                if (isFirstEmission) {
                    isFirstEmission = false
                    return@collect
                }
                loadDynamicSections()
            }
        }
    }

    fun refresh() {
        scope.launch {
            _isRefreshing.value = true
            loadImages()
            loadDynamicSections()
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

    private fun loadDynamicSections() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, categoryDisplayType = CategoryDisplayType.MAIN) }

            val sections = getSearchTabs()
                .filter { it.enabled }
                .mapNotNull { config ->
                    val items = loadSectionItems(config)
                    if (items.isEmpty()) {
                        null
                    } else {
                        CategoryDynamicSection(
                            id = config.id,
                            title = config.title,
                            type = config.type,
                            source = config.source,
                            sourceSlug = config.sourceSlug,
                            viewType = config.viewType,
                            items = items,
                            moreTitle = config.more?.title,
                            moreLink = config.more?.link,
                        )
                    }
                }

            _uiState.update {
                it.copy(
                    dynamicSections = sections,
                    isLoading = false,
                    error = null,
                )
            }
        }
    }

    private suspend fun loadSectionItems(config: SearchTabConfig): List<DisplayableTerm> {
        val maxItems = config.max
        return when (config.type.lowercase()) {
            "brand_ids" -> loadBrandsBySource(config.source, maxItems)
            "attribute", "attributes" -> {
                val attributeId = config.source.toIntOrNull() ?: return emptyList()
                loadAttributeTermsBySource(attributeId, maxItems)
            }

            else -> emptyList()
        }
    }

    private suspend fun loadBrandsBySource(source: String, maxItems: Int?): List<Brand> {
        val includeIds = source.split(",").mapNotNull { it.trim().toIntOrNull() }
        val perPage = maxItems ?: includeIds.size.takeIf { it > 0 } ?: 50
        val queries = buildMap {
            if (source.isNotBlank()) put("include", source)
            put("per_page", perPage.toString())
        }

        var brands: List<Brand> = emptyList()
        getBrands(queries)
            .onEach { result ->
                if (result is Result.Success) {
                    brands = result.data
                }
            }
            .collect()

        if (includeIds.isNotEmpty()) {
            val orderMap = includeIds.withIndex().associate { (index, id) -> id to index }
            brands = brands.sortedBy { orderMap[it.id] ?: Int.MAX_VALUE }
        }
        return if (maxItems != null) brands.take(maxItems) else brands
    }

    private suspend fun loadAttributeTermsBySource(
        attributeId: Int,
        maxItems: Int?,
    ): List<AttributeTerm> {
        val perPage = maxItems ?: 100
        val queries = mapOf("per_page" to perPage.toString())

        var terms: List<AttributeTerm> = emptyList()
        getAttributeTerms(attributeId, queries)
            .onEach { result ->
                if (result is Result.Success) {
                    terms = result.data.sortedBy { it.menuOrder }
                }
            }
            .collect()
        return if (maxItems != null) terms.take(maxItems) else terms
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

    fun toProductListArgsForItem(section: CategoryDynamicSection, id: Int, title: String): Map<String, String>? =
        when (section.type.lowercase()) {
            "brand_ids" -> mapOf(
                PRODUCT_ARG_BRAND_ID to id.toString(),
                PRODUCT_ARG_TITLE to title,
            )

            "attribute", "attributes" -> mapOf(
                PRODUCT_ARG_ATTRIBUTE to buildAttributeQueryKey(
                    attributeSource = section.source,
                    attributeSlug = section.sourceSlug,
                ),
                PRODUCT_ARG_ATTRIBUTE_TERM to id.toString(),
                PRODUCT_ARG_TITLE to title,
            )

            else -> null
        }

    fun toProductListArgsForMore(section: CategoryDynamicSection): Map<String, String>? {
        val link = section.moreLink ?: return null
        return when (link.type) {
            LinkType.ALL_PRODUCTS -> mapOf(PRODUCT_ARG_TITLE to (link.title ?: section.title))
            else -> link.getRouteQuery().takeIf { it.isNotEmpty() }
        }
    }

    fun showAllItemsFromMore(section: CategoryDynamicSection) {
        val link = section.moreLink ?: return
        when (link.type) {
            LinkType.ALL_BRANDS -> showAllBrands(section.moreTitle ?: section.title)
            LinkType.ATTRIBUTES -> {
                val source = link.target.ifBlank { section.source }
                val attributeId = source.toIntOrNull() ?: return
                showAllAttributes(
                    title = section.moreTitle ?: section.title,
                    attributeId = attributeId,
                    attributeSource = source,
                    attributeFilterKey = buildAttributeQueryKey(
                        attributeSource = section.source,
                        attributeSlug = section.sourceSlug,
                    ),
                )
            }

            else -> Unit
        }
    }

    private fun showAllBrands(title: String) {
        loadBrands(BrandListType.All) { brands ->
            _uiState.update {
                it.copy(
                    allItemsState = CategoryAllItemsState(
                        kind = CategoryAllItemsKind.BRAND,
                        title = title,
                        items = brands,
                    ),
                    categoryDisplayType = CategoryDisplayType.ALL_ITEMS,
                )
            }
        }
    }

    private fun showAllAttributes(
        title: String,
        attributeId: Int,
        attributeSource: String,
        attributeFilterKey: String,
    ) {
        scope.launch {
            val terms = loadAttributeTermsBySource(attributeId = attributeId, maxItems = 100)
            _uiState.update {
                it.copy(
                    allItemsState = CategoryAllItemsState(
                        kind = CategoryAllItemsKind.ATTRIBUTE,
                        title = title,
                        items = terms,
                        attributeSource = attributeSource,
                        attributeFilterKey = attributeFilterKey,
                    ),
                    categoryDisplayType = CategoryDisplayType.ALL_ITEMS,
                )
            }
        }
    }

    private fun buildAttributeQueryKey(attributeSource: String, attributeSlug: String?): String {
        val slug = attributeSlug
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: attributeSource.trim().takeIf { value -> value.isNotEmpty() && value.toIntOrNull() == null }
        if (slug == null) return attributeSource
        return if (slug.startsWith("pa_")) slug else "pa_$slug"
    }

    fun backToMainDisplay() {
        _uiState.update {
            it.copy(
                categoryDisplayType = CategoryDisplayType.MAIN,
                allItemsState = null,
            )
        }
    }

    fun clear() {
        searchJob?.cancel()
        scope.cancel()
    }
}
