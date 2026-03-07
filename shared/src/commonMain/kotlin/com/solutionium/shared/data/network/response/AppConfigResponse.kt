package com.solutionium.shared.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppConfigResponse (
    val status: String? = null,
    val message: String? = null,

    @SerialName("header_logo")
    val headerLogo: String? = null,

    @SerialName("stories")
    val stories: List<StoryItemR>? = null,

    @SerialName("home_banner")
    val homeBanners: List<HomeBanner>? = null,

    @SerialName("payment_discount")
    val paymentDiscount: List<PaymentDiscount>? = null,

    @SerialName("payment_force_enabled")
    val paymentForceEnabled: List<String>? = null,

    @SerialName("bacs_details")
    val bacsDetailsResponse: BACSDetailsResponse? = null,

    val images: List<ConfigImage>? = null,

    @SerialName("free_shipping_method_id")
    val freeShippingMethodID: String? = null,

    @SerialName("review_criteria")
    val reviewCriteria: List<ReviewCriteriaResponse>? = null,

    @SerialName("app_version")
    val appVersion: AppVersionResponse? = null,

    @SerialName("contact")
    val contact: ContactResponse? = null,

    @SerialName("search_tab")
    val searchTabs: List<SearchTabResponse>? = null,
)

@Serializable
data class ConfigImage (
    @SerialName("term_id")
    val termID: Int,

    val src: String? = null
)

@Serializable
data class PaymentDiscount (
    @SerialName("method_id")
    val methodID: String? = null,

    val amount: Double? = null
)

@Serializable
data class HomeBanner (
    val id: Int? = null,
    val title: String? = null,
    @SerialName("subtitle")
    val subTitle: String? = null,
    val link: ConfigLink? = null,
    val src: String? = null
)

@Serializable
data class ConfigLink (
    val title: String? = null,
    val type: String? = null,
    val target: String? = null
)

@Serializable
data class StoryItemR (
    val id: Int,
    val title: String? = null,
    val subtitle: String? = null,
    @SerialName("media_url")
    val mediaUrl: String? = null,
    val link: ConfigLink? = null
)

@Serializable
data class BACSDetailsResponse (
    @SerialName("card_number")
    val cardNumber: String? = null,

    @SerialName("iban_number")
    val ibanNumber: String? = null,

    @SerialName("account_holder")
    val accountHolder: String? = null,

    @SerialName("contact_number")
    val contactNumber: String? = null
)

@Serializable
data class ReviewCriteriaResponse (
    @SerialName("cat_id")
    val catID: Int,

    val criteria: List<String>
)

@Serializable
data class AppVersionResponse(

    @SerialName("latest_version")
    val latestVersion: String,

    @SerialName("min_required_version")
    val minRequiredVersion: String
)

@Serializable
data class ContactResponse (
    val call: String?,
    val whatsapp: String?,
    val instagram: String?,
    val telegram: String?,
    val email: String?
)

@Serializable
data class SearchTabResponse(
    val id: Int,
    val enabled: Boolean? = null,
    val title: String? = null,
    val type: String? = null,
    val source: String? = null,
    val max: Int? = null,
    @SerialName("veiw_type")
    val viewTypeTypo: String? = null,
    @SerialName("view_type")
    val viewType: String? = null,
    val more: SearchTabMoreResponse? = null,
)

@Serializable
data class SearchTabMoreResponse(
    val title: String? = null,
    val link: ConfigLink? = null,
)
