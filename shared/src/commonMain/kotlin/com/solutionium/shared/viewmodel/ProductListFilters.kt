package com.solutionium.shared.viewmodel

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
    fun buildFilterCriteria(args: Map<String, String>): MutableList<FilterCriterion> {
        args[PRODUCT_ARG_BRAND_ID]?.let {
            filters.add(FilterCriterion(ProductFilterKey.BRAND_ID.apiKey, it))
        }

        val attribute = args[PRODUCT_ARG_ATTRIBUTE]
        val attributeTerm = args[PRODUCT_ARG_ATTRIBUTE_TERM]
        if (attribute != null && attributeTerm != null) {
            filters.add(FilterCriterion(ProductFilterKey.ATTRIBUTE.apiKey, attribute))
            filters.add(FilterCriterion(ProductFilterKey.ATTRIBUTE_TERM.apiKey, attributeTerm))
        }

        args[PRODUCT_ARG_IDS]?.takeIf { it.isNotEmpty() }?.let {
            filters.add(FilterCriterion(ProductFilterKey.INCLUDE_IDS.apiKey, it))
        }

        args[PRODUCT_ARG_CATEGORY]?.let {
            filters.add(FilterCriterion(ProductFilterKey.CATEGORY_ID.apiKey, it))
        }
        args[PRODUCT_ARG_TAG]?.let {
            filters.add(FilterCriterion(ProductFilterKey.TAG.apiKey, it))
        }
        args[PRODUCT_ARG_SEARCH]?.let {
            filters.add(FilterCriterion(ProductFilterKey.SEARCH.apiKey, it))
        }
        args[PRODUCT_ARG_FEATURED]?.let {
            filters.add(FilterCriterion(ProductFilterKey.FEATURED.apiKey, it))
        }
        args[PRODUCT_ARG_ON_SALE]?.let {
            filters.add(FilterCriterion(ProductFilterKey.ON_SALE.apiKey, it))
        }

        return filters
    }
}
