package com.solutionium.shared.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewCriteriaEnvelopeResponse(
    val success: Boolean? = null,
    val data: ReviewCriteriaDataResponse? = null,
)

@Serializable
data class ReviewCriteriaDataResponse(
    val criteria: List<String>? = null,
    @SerialName("source")
    val source: String? = null,
)
