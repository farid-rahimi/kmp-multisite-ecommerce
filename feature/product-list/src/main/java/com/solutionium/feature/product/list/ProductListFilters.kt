package com.solutionium.feature.product.list

import androidx.lifecycle.SavedStateHandle
import com.solutionium.shared.data.model.FilterCriterion
import com.solutionium.shared.data.model.PRODUCT_ARG_ATTRIBUTE
import com.solutionium.shared.data.model.PRODUCT_ARG_ATTRIBUTE_TERM
import com.solutionium.shared.data.model.PRODUCT_ARG_BRAND_ID
import com.solutionium.shared.data.model.PRODUCT_ARG_CATEGORY
import com.solutionium.shared.data.model.PRODUCT_ARG_FEATURED
import com.solutionium.shared.data.model.PRODUCT_ARG_IDS
import com.solutionium.shared.data.model.PRODUCT_ARG_ON_SALE
import com.solutionium.shared.data.model.PRODUCT_ARG_SEARCH
import com.solutionium.shared.data.model.PRODUCT_ARG_TAG
import com.solutionium.shared.data.model.ProductFilterKey

internal class ProductListFilters(
    private val filters: MutableList<FilterCriterion> = mutableListOf(),
) {
    fun buildFilterCriteria(savedStateHandle: SavedStateHandle): MutableList<FilterCriterion> {
        val brandId: String? = savedStateHandle.get(PRODUCT_ARG_BRAND_ID)
        brandId?.let { filters.add(FilterCriterion(ProductFilterKey.BRAND_ID.apiKey, it)) }

        val attribute: String? = savedStateHandle.get(PRODUCT_ARG_ATTRIBUTE)
        val attributeTerm: String? = savedStateHandle.get(PRODUCT_ARG_ATTRIBUTE_TERM)
        if (attribute != null && attributeTerm != null) {
            filters.add(FilterCriterion(ProductFilterKey.ATTRIBUTE.apiKey, attribute))
            filters.add(FilterCriterion(ProductFilterKey.ATTRIBUTE_TERM.apiKey, attributeTerm))
        }

        val productIds: String? = savedStateHandle.get(PRODUCT_ARG_IDS)
        productIds?.takeIf { it.isNotEmpty() }?.let {
            filters.add(FilterCriterion(ProductFilterKey.INCLUDE_IDS.apiKey, it))
        }

        val categoryId: String? = savedStateHandle.get(PRODUCT_ARG_CATEGORY)
        categoryId?.let { filters.add(FilterCriterion(ProductFilterKey.CATEGORY_ID.apiKey, it)) }

        val tagId: String? = savedStateHandle.get(PRODUCT_ARG_TAG)
        tagId?.let { filters.add(FilterCriterion(ProductFilterKey.TAG.apiKey, it)) }

        val search: String? = savedStateHandle.get(PRODUCT_ARG_SEARCH)
        search?.let { filters.add(FilterCriterion(ProductFilterKey.SEARCH.apiKey, it)) }

        val featured: String? = savedStateHandle.get(PRODUCT_ARG_FEATURED)
        featured?.let { filters.add(FilterCriterion(ProductFilterKey.FEATURED.apiKey, it)) }

        val onSale: String? = savedStateHandle.get(PRODUCT_ARG_ON_SALE)
        onSale?.let { filters.add(FilterCriterion(ProductFilterKey.ON_SALE.apiKey, it)) }

        return filters
    }
}
