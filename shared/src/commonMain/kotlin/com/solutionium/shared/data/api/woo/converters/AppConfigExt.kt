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

fun AppConfigResponse.toModel() = AppConfig(
    message = message,

    headerLogoUrl = headerLogo,

    stories = stories?.map { it.toModel() } ?: emptyList(),

    homeBanners = homeBanners?.map { it.toModel() } ?: emptyList(),

    paymentDiscount = paymentDiscount?.associate { it.methodID.orEmpty() to (it.amount ?: 0.0) }
        ?: emptyMap(),
    paymentForceEnabled = paymentForceEnabled ?: emptyList(),
    bacsDetails = bacsDetailsResponse?.toModel(),
    images = images?.associate { it.termID to (it.src.orEmpty()) } ?: emptyMap(),
    freeShippingMethodID = freeShippingMethodID,
    reviewCriteria = reviewCriteria?.map {
        ReviewCriteria(
            catID = it.catID,
            criteria = it.criteria
        )
    } ?: emptyList(),
    appVersion = appVersion?.toModel(),
    contact = contact?.toModel(),
    searchTabs = searchTabs?.map { it.toModel() } ?: emptyList(),
)

fun HomeBanner.toModel() = BannerItem(
    id = id ?: 0,
    title = title.orEmpty(),
    subTitle = subTitle,
    link = link?.toModel(),
    imageUrl = src.orEmpty()
)

fun ConfigLink.toModel() = Link(
    title = title,
    type = LinkType.fromValue(type.orEmpty()) ?: LinkType.EXTERNAL,
    target = target.orEmpty()
)

fun StoryItemR.toModel() = StoryItem(
    id = id,
    title = title.orEmpty(),
    subtitle = subtitle,
    mediaUrl = mediaUrl.orEmpty(),
    link = link?.toModel()
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

fun SearchTabResponse.toModel() = SearchTabConfig(
    id = id,
    enabled = enabled == true,
    title = title.orEmpty(),
    type = type.orEmpty().trim().lowercase(),
    source = source.orEmpty(),
    max = max,
    viewType = SearchTabViewType.fromValue(viewType ?: viewTypeTypo),
    more = more?.toModel(),
)

fun SearchTabMoreResponse.toModel() = SearchTabMore(
    title = title,
    link = link?.toModel(),
)
