package com.solutionium.shared.data.model

data class BannerItem(
    val id: Int,
    val imageUrl: String,
    val title: String? = null, // Optional title to display on the banner
    val subTitle: String? = null, // Optional: For navigation when clicked
    val link: Link? = null // Optional: Link to navigate when clicked

) {



}

data class Link(
    val title: String? = null,
    val type: LinkType, //product, products, attribute_term, brand, category, tag, external
    val target: String
) {
    val isProductLink: Boolean
        get() = type == LinkType.PRODUCT

    val isProductsLink: Boolean
        get() = type in listOf(
            LinkType.PRODUCTS,
            LinkType.ATTRIBUTE_TERM,
            LinkType.BRAND,
            LinkType.CATEGORY,
            LinkType.TAG,
            LinkType.ALL_PRODUCTS,
        )

    val isExternalLink: Boolean
        get() = type == LinkType.EXTERNAL

    fun getRouteQuery(): Map<String, String> {
        return when (type) {
            //LinkType.PRODUCT -> mapOf()
            LinkType.PRODUCTS -> mapOf(PRODUCT_ARG_TITLE to (title ?: ""), PRODUCT_ARG_IDS to target)
            LinkType.ATTRIBUTE_TERM -> {
                val (attributeRaw, termId) = parseAttributeTarget(target)
                if (attributeRaw.isBlank() || termId.isBlank()) return emptyMap()
                mapOf(
                    PRODUCT_ARG_TITLE to (title ?: ""),
                    PRODUCT_ARG_ATTRIBUTE to normalizeAttributeFilterKey(attributeRaw),
                    PRODUCT_ARG_ATTRIBUTE_TERM to termId,
                )
            }
            LinkType.BRAND -> mapOf(PRODUCT_ARG_TITLE to (title ?: ""), PRODUCT_ARG_BRAND_ID to target)
            LinkType.CATEGORY -> mapOf(PRODUCT_ARG_TITLE to (title ?: ""), PRODUCT_ARG_CATEGORY to target)
            LinkType.TAG -> mapOf(PRODUCT_ARG_TITLE to (title ?: ""), PRODUCT_ARG_TAG to target)
            LinkType.ALL_PRODUCTS -> mapOf(PRODUCT_ARG_TITLE to (title ?: ""))
            //LinkType.EXTERNAL -> link.target // Direct URL for external links
            else -> emptyMap()
        }
    }

    private fun normalizeAttributeFilterKey(rawValue: String): String {
        val value = rawValue.trim()
        if (value.isEmpty()) return value
        if (value.startsWith("pa_")) return value
        return if (value.toIntOrNull() == null) "pa_$value" else value
    }

    private fun parseAttributeTarget(rawTarget: String): Pair<String, String> {
        val value = rawTarget.trim()
        if (value.isEmpty()) return "" to ""
        // New format: "<attribute_slug>:<term_id>"
        if (':' in value) {
            val parts = value.split(":", limit = 2)
            return parts.getOrNull(0).orEmpty().trim() to parts.getOrNull(1).orEmpty().trim()
        }
        // Backward compatibility: "<attribute>,<term_id>"
        if (',' in value) {
            val parts = value.split(",", limit = 2)
            return parts.getOrNull(0).orEmpty().trim() to parts.getOrNull(1).orEmpty().trim()
        }
        return "" to ""
    }
}

enum class LinkType(val value: String) {
    PRODUCT("product"),
    PRODUCTS("products"),
    ATTRIBUTE_TERM("attribute_term"),
    BRAND("brand"),
    CATEGORY("category"),
    TAG("tag"),
    ALL_PRODUCTS("all_products"),
    ALL_BRANDS("all_brands"),
    ATTRIBUTES("attributes"),
    EXTERNAL("external");

    companion object {
        fun fromValue(value: String): LinkType? {
            val normalized = value.trim().lowercase()
            return when (normalized) {
                "attribute" -> ATTRIBUTE_TERM
                else -> entries.find { it.value == normalized }
            }
        }
    }
}
