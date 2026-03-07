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
            LinkType.ATTRIBUTE_TERM -> mapOf(PRODUCT_ARG_TITLE to (title ?: ""), PRODUCT_ARG_ATTRIBUTE to target.split(",")[0], PRODUCT_ARG_ATTRIBUTE_TERM to target.split(",")[1])
            LinkType.BRAND -> mapOf(PRODUCT_ARG_TITLE to (title ?: ""), PRODUCT_ARG_BRAND_ID to target)
            LinkType.CATEGORY -> mapOf(PRODUCT_ARG_TITLE to (title ?: ""), PRODUCT_ARG_CATEGORY to target)
            LinkType.TAG -> mapOf(PRODUCT_ARG_TITLE to (title ?: ""), PRODUCT_ARG_TAG to target)
            LinkType.ALL_PRODUCTS -> mapOf(PRODUCT_ARG_TITLE to (title ?: ""))
            //LinkType.EXTERNAL -> link.target // Direct URL for external links
            else -> emptyMap()
        }
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
            return entries.find { it.value == value }
        }
    }
}
