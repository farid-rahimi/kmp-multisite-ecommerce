package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.BannerItem
import com.solutionium.shared.data.model.CartItem
import com.solutionium.shared.data.model.ContactInfo
import com.solutionium.shared.data.model.ProductThumbnail
import com.solutionium.shared.data.model.StoryItem

enum class UpdateType {
    NONE,
    RECOMMENDED,
    FORCED,
}

data class UpdateInfo(
    val type: UpdateType = UpdateType.NONE,
    val latestVersionName: String = "",
)

data class HomeUiState(
    val updateInfo: UpdateInfo = UpdateInfo(),
    val contactInfo: ContactInfo? = null,
    val showContactSupportDialog: Boolean = false,
    val isLoading: Boolean,
    val headerLogoUrl: String? = null,
    val serverStoryItems: List<StoryItem> = emptyList(),
    val storiesLoading: Boolean = true,
    val storyItems: List<StoryItem> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val favoriteIds: List<Int> = emptyList(),
    val paymentDiscount: Double? = null,
    val installmentPriceEnabled: Boolean = false,
    val newArrivals: List<ProductThumbnail> = emptyList(),
    val newArrivalsLoading: Boolean = true,
    val appOffers: List<ProductThumbnail> = emptyList(),
    val appOffersLoading: Boolean = true,
    val featured: List<ProductThumbnail> = emptyList(),
    val featuredLoading: Boolean = true,
    val onSales: List<ProductThumbnail> = emptyList(),
    val onSalesLoading: Boolean = true,
    val isLogin: Boolean = false,
    val isSuperUser: Boolean = false,
) {
    fun cartItemCount(productId: Int): Int =
        cartItems.find { it.productId == productId && it.variationId == 0 }?.quantity ?: 0

    fun isFavorite(productId: Int): Boolean =
        favoriteIds.contains(productId)

    fun discountedPrice(originalPrice: Double?): Double? =
        originalPrice?.let { paymentDiscount?.let { (100 - it) / 100 }?.let { it * originalPrice } }
}

data class BannerSliderState(
    val banners: List<BannerItem> = emptyList(),
    val isLoading: Boolean = false,
)
