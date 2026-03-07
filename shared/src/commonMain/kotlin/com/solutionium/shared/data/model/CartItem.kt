package com.solutionium.shared.data.model

import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
data class CartItem(
    val productId: Int,
    val variationId: Int = 0,
    var quantity: Int,
    val isDecant: Boolean = false,
    val decVol: String? = null,
    val categoryIDs: List<Int> = emptyList(),
    val brandIDs: List<Int> = emptyList(),
    val name: String,
    val variationAttributes: List<VariationAttribute> = emptyList(), // For variations, store selected attributes
    var currentPrice: Double,
    var regularPrice: Double? = null,
    var currentStock: Int?,      // Stock at the time of adding or last validation
    val manageStock: Boolean = true,
    val stockStatus: String = "instock",
    val addedAt: Long = Clock.System.now().toEpochMilliseconds(),
    // Optional: For more detailed info or if variations are complex

    val imageUrl: String,

    var requiresAttention: Boolean = false,
    val validationInfo: ValidationInfo? = null,
    val shippingClass: String = "",
    val appOffer: Double = 0.0
) {
    val isOnSale: Boolean
        get() = (regularPrice != null && currentPrice < regularPrice!!) || appOffer > 0

//    val price: Double =
//        if (appOffer > 0 && regularPrice == null) {
//            currentPrice * (1 - appOffer / 100)
//        } else {
//            currentPrice
//        }
//
//    val rPrice: Double? =
//        if (appOffer > 0 && regularPrice == null) {
//            currentPrice
//        } else {
//            regularPrice
//        }

}


fun ProductDetail.toCartItem(variation: ProductVariation) = CartItem(
    productId = id,
    variationId = variation.id,
    name = name,
    categoryIDs = categories.map { it.id },
    brandIDs = brands.map { it.id },
    variationAttributes = variation.attributes,
    currentPrice = variation.price,
    regularPrice = if (variation.onSale) variation.regularPrice else null,
    manageStock = variation.manageStock,
    stockStatus = variation.stockStatus,
    quantity = 1,

    imageUrl = variation.image ?: imageUrls.firstOrNull() ?: "",

    currentStock = if (variation.manageStock) variation.stock else null,
    shippingClass = shippingClass ?: "",
    appOffer = appOffer
)

fun ProductDetail.toCartItem() = CartItem(
    productId = id,
    variationId = 0,
    name = name,
    categoryIDs = categories.map { it.id },
    brandIDs = brands.map { it.id },
    currentPrice = price,
    regularPrice = if (onSale) regularPrice else null,
    manageStock = manageStock,
    stockStatus = stockStatus,
    quantity = 1,
    imageUrl = imageUrls.firstOrNull() ?: "",
    currentStock = if (manageStock) stock else null,
    shippingClass = shippingClass ?: "",
    appOffer = appOffer
)

fun ProductThumbnail.toCartItem() = CartItem(
    productId = id,
    variationId = 0,
    name = name,
    categoryIDs = categories.map { it.id },
    brandIDs = brands.map { it.id },
    currentPrice = price,
    regularPrice = regularPrice,
    manageStock = manageStock,
    stockStatus = stockStatus,
    quantity = 1,
    imageUrl = imageUrl,
    currentStock = if (manageStock) stock else null,
    shippingClass = shippingClass ?: "",
    appOffer = appOffer
)
