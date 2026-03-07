package com.solutionium.shared.data.api.woo.converters

import com.solutionium.shared.data.model.AppConfig
import com.solutionium.shared.data.model.AppVersion
import com.solutionium.shared.data.model.BACSDetails
import com.solutionium.shared.data.model.BannerItem
import com.solutionium.shared.data.model.ContactInfo
import com.solutionium.shared.data.model.Link
import com.solutionium.shared.data.model.LinkType
import com.solutionium.shared.data.model.ReviewCriteria
import com.solutionium.shared.data.model.SearchTabConfig
import com.solutionium.shared.data.model.SearchTabMore
import com.solutionium.shared.data.model.SearchTabViewType
import com.solutionium.shared.data.model.StoryItem
import com.solutionium.shared.data.network.response.AppConfigResponse
import com.solutionium.shared.data.network.response.AppVersionResponse
import com.solutionium.shared.data.network.response.BACSDetailsResponse
import com.solutionium.shared.data.network.response.ConfigLink
import com.solutionium.shared.data.network.response.ContactResponse
import com.solutionium.shared.data.network.response.HomeBanner
import com.solutionium.shared.data.network.response.SearchTabMoreResponse
import com.solutionium.shared.data.network.response.SearchTabResponse
import com.solutionium.shared.data.network.response.StoryItemR

fun AppConfigResponse.toModel(language: String?): AppConfig {
    val selectedLanguage = language.orEmpty().ifBlank { "en" }
    return AppConfig(
        message = message,

        headerLogoUrl = headerLogo,

        stories = stories?.map { it.toModel(selectedLanguage) } ?: emptyList(),

        homeBanners = homeBanners?.map { it.toModel(selectedLanguage) } ?: emptyList(),

        paymentDiscount = paymentDiscount?.associate { it.methodID.orEmpty() to (it.amount ?: 0.0) }
            ?: emptyMap(),
        paymentForceEnabled = paymentForceEnabled ?: emptyList(),
        bacsDetails = bacsDetailsResponse?.toModel(),
        images = images?.associate { it.termID to (it.src.orEmpty()) } ?: emptyMap(),
        freeShippingMethodID = freeShippingMethodID,
        reviewCriteria = reviewCriteria?.map {
            ReviewCriteria(
                catID = it.catID,
                criteria = localizeList(
                    language = selectedLanguage,
                    en = it.criteria,
                    ar = it.criteriaAr,
                    fa = it.criteriaFa,
                ),
            )
        } ?: emptyList(),
        appVersion = appVersion?.toModel(),
        contact = contact?.toModel(),
        searchTabs = searchTabs?.map { it.toModel(selectedLanguage) } ?: emptyList(),
    )
}

fun HomeBanner.toModel(language: String) = BannerItem(
    id = id ?: 0,
    title = localizeText(language, en = title, ar = titleAr, fa = titleFa),
    subTitle = localizeTextOrNull(language, en = subTitle, ar = subTitleAr, fa = subTitleFa),
    link = link?.toModel(language),
    imageUrl = src.orEmpty(),
)

fun ConfigLink.toModel(language: String) = Link(
    title = localizeTextOrNull(language, en = title, ar = titleAr, fa = titleFa),
    type = LinkType.fromValue(type.orEmpty()) ?: LinkType.EXTERNAL,
    target = target.orEmpty(),
)

fun StoryItemR.toModel(language: String) = StoryItem(
    id = id,
    title = localizeText(language, en = title, ar = titleAr, fa = titleFa),
    subtitle = localizeTextOrNull(language, en = subtitle, ar = subtitleAr, fa = subtitleFa),
    mediaUrl = mediaUrl.orEmpty(),
    link = link?.toModel(language),
)

fun BACSDetailsResponse.toModel() = BACSDetails(
    cardNumber = cardNumber,
    ibanNumber = ibanNumber,
    accountHolder = accountHolder,
    contactNumber = contactNumber
)

fun AppVersionResponse.toModel() = AppVersion(
    latestVersion = latestVersion,
    minRequiredVersion = minRequiredVersion
)

fun ContactResponse.toModel() = ContactInfo(
    call = call.orEmpty(),
    whatsapp = whatsapp.orEmpty(),
    instagram = instagram.orEmpty(),
    telegram = telegram.orEmpty(),
    email = email.orEmpty()
)

fun SearchTabResponse.toModel(language: String) = SearchTabConfig(
    id = id,
    enabled = enabled == true,
    title = localizeText(language, en = title, ar = titleAr, fa = titleFa),
    type = type.orEmpty().trim().lowercase(),
    source = source.orEmpty(),
    max = max,
    viewType = SearchTabViewType.fromValue(viewType ?: viewTypeTypo),
    more = more?.toModel(language),
)

fun SearchTabMoreResponse.toModel(language: String) = SearchTabMore(
    title = localizeTextOrNull(language, en = title, ar = titleAr, fa = titleFa),
    link = link?.toModel(language),
)

private fun localizeText(language: String, en: String?, ar: String?, fa: String?): String {
    return when (language.lowercase()) {
        "ar" -> ar?.takeIf { it.isNotBlank() } ?: en.orEmpty()
        "fa" -> fa?.takeIf { it.isNotBlank() } ?: en.orEmpty()
        else -> en.orEmpty()
    }
}

private fun localizeTextOrNull(language: String, en: String?, ar: String?, fa: String?): String? {
    val value = localizeText(language, en = en, ar = ar, fa = fa)
    return value.takeIf { it.isNotBlank() }
}

private fun localizeList(
    language: String,
    en: List<String>?,
    ar: List<String>?,
    fa: List<String>?,
): List<String> {
    val byLanguage = when (language.lowercase()) {
        "ar" -> ar
        "fa" -> fa
        else -> en
    }
    return if (!byLanguage.isNullOrEmpty()) byLanguage else en.orEmpty()
}
