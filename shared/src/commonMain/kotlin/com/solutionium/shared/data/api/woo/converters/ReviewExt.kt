package com.solutionium.shared.data.api.woo.converters

import com.solutionium.shared.data.model.CriteriaRating
import com.solutionium.shared.data.model.NewReview
import com.solutionium.shared.data.model.Review
import com.solutionium.shared.data.model.ReviewChild
import com.solutionium.shared.data.network.request.ReviewRequest
import com.solutionium.shared.data.network.response.CriteriaRatingResponse
import com.solutionium.shared.data.network.response.ReviewChildResponse
import com.solutionium.shared.data.network.response.WooReviewResponse

fun WooReviewResponse.toModel() = Review(
    id = id,
    dateCreated = dateCreated,
    productID = productID,
    productName = productName,
    productPermalink = productPermalink,
    status = status,
    reviewer = reviewer,
    reviewerEmail = reviewerEmail,
    review = review,
    rating = rating,
    verified = verified || verifiedBuyer,
    featured = featured,
    helpful = helpful || helpfulVotes > 0,
    helpfulVotes = helpfulVotes,
    criteriaRatings = criteriaRatings.map { it.toModel() },
    children = children.map { it.toModel() }
)

fun CriteriaRatingResponse.toModel() = CriteriaRating(
    label = label,
    value = value
)

fun ReviewChildResponse.toModel() = ReviewChild(
    id = id,
    author = author,
    content = content,
    date = date,
    avatar = avatar
)

fun NewReview.toRequestBody() = ReviewRequest(
    productID = productID,
    status = "hold",
    reviewer = reviewer,
    reviewerEmail = reviewerEmail,
    review = review,
    rating = rating,
    criteriaRatings = criteriaRatings.map {
        CriteriaRatingResponse(
            label = it.label,
            value = it.value
        )
    }
)
