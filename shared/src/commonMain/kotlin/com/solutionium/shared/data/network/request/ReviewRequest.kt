package com.solutionium.shared.data.network.request

import com.solutionium.shared.data.network.response.CriteriaRatingResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewRequest(

    @SerialName("product_id")
    val productID: Int,

    val status: String,
    val reviewer: String,

    @SerialName("reviewer_email")
    val reviewerEmail: String,
    val review: String,

    val rating: Int,

    @SerialName("criteria_ratings")
    val criteriaRatings: List<CriteriaRatingResponse> = emptyList(),

)
