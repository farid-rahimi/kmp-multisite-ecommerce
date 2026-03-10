package com.solutionium.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AppConfig (

    val message: String? = null,

    val headerLogoUrl: String? = null,

    val stories: List<StoryItem> = emptyList(),

    val homeBanners: List<BannerItem> = emptyList(),

    val paymentDiscount: Map<String, Double> = emptyMap(),

    val paymentForceEnabled: List<String> = emptyList(),

    val installmentPrice: Boolean = false,
    val walletEnabled: Boolean = false,

    val bacsDetails: BACSDetails? = null,

    val images: Map<Int, String> = emptyMap(),

    val freeShippingMethodID: String? = null,

    val reviewCriteria: List<ReviewCriteria> = emptyList(),

    val appVersion: AppVersion? = null,

    val contact: ContactInfo? = null,

    val searchTabs: List<SearchTabConfig> = emptyList(),

)


data class BACSDetails (
    val cardNumber: String? = null,

    val ibanNumber: String? = null,

    val accountHolder: String? = null,

    val contactNumber: String? = null
)

data class ReviewCriteria (
    val catID: Int,

    val criteria: List<String>
)

data class ContactInfo (
    val call: String,
    val whatsapp: String,
    val instagram: String,
    val telegram: String,
    val email: String
)

data class SearchTabConfig(
    val id: Int,
    val enabled: Boolean,
    val title: String,
    val type: String,
    val source: String,
    val sourceSlug: String? = null,
    val max: Int?,
    val viewType: SearchTabViewType,
    val more: SearchTabMore?,
)

data class SearchTabMore(
    val title: String?,
    val link: Link?,
)

enum class SearchTabViewType(val value: String) {
    SPOTLIGHT("spotlight"),
    CIRCLE_ROW("circle_row"),
    GRID("grid");

    companion object {
        fun fromValue(value: String?): SearchTabViewType =
            entries.firstOrNull { it.value == value?.trim()?.lowercase() } ?: CIRCLE_ROW
    }
}
