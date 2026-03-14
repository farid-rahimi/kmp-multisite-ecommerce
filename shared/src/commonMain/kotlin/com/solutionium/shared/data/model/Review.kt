package com.solutionium.shared.data.model

data class Review(
    val id: Int,

    val dateCreated: String,

    val productID: Int,

    val productName: String,

    val productPermalink: String,

    val status: String,
    val reviewer: String,

    val reviewerEmail: String,

    val review: String,
    val rating: Int,
    val verified: Boolean,
    val featured: Boolean,
    val helpful: Boolean,
    val helpfulVotes: Int,

    val criteriaRatings: List<CriteriaRating>,

    val children: List<ReviewChild>
)

data class CriteriaRating(
    val label: String,
    val value: Int
)

data class ReviewChild(
    val id: String,
    val author: String,
    val content: String,
    val date: String,
    val avatar: String,
)

data class NewReview(
    val productID: Int,
    val reviewer: String,
    val reviewerEmail: String,
    val review: String,
    val rating: Int,
    val criteriaRatings: List<CriteriaRating>
)
