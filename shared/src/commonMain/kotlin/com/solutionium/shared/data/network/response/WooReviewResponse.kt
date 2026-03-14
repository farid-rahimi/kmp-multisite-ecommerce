package com.solutionium.shared.data.network.response
import kotlinx.serialization.*

typealias WooReviewListResponse = List<WooReviewResponse>

@Serializable
data class WooReviewResponse (
    val id: Int = 0,

    @SerialName("date_created")
    val dateCreated: String = "",

    @SerialName("date_created_gmt")
    val dateCreatedGmt: String = "",

    @SerialName("product_id")
    val productID: Int = 0,

    @SerialName("product_name")
    val productName: String = "",

    @SerialName("product_permalink")
    val productPermalink: String = "",

    val status: String = "hold",
    val reviewer: String = "",

    @SerialName("reviewer_email")
    val reviewerEmail: String = "",

    val review: String = "",
    val rating: Int = 0,
    val verified: Boolean = false,
    @SerialName("verified_buyer")
    val verifiedBuyer: Boolean = false,
    val featured: Boolean = false,
    val helpful: Boolean = false,
    @SerialName("helpful_votes")
    val helpfulVotes: Int = 0,

    @SerialName("criteria_ratings")
    val criteriaRatings: List<CriteriaRatingResponse> = emptyList(),

    val children: List<ReviewChildResponse> = emptyList()

)

@Serializable
data class CriteriaRatingResponse (
    val label: String,
    val value: Int
)

@Serializable
data class ReviewChildResponse (
    val id: String,
    val author: String,
    val content: String,
    val date: String,
    val avatar: String,
)
