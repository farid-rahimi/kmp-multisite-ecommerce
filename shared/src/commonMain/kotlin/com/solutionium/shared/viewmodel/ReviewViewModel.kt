package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.CriteriaRating
import com.solutionium.shared.data.model.FilterCriterion
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.NewReview
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.model.UserDetails
import com.solutionium.shared.domain.config.ReviewCriteriaUseCase
import com.solutionium.shared.domain.review.GetReviewListPagingUseCase
import com.solutionium.shared.domain.review.SubmitReviewUseCase
import com.solutionium.shared.domain.user.CheckLoginUserUseCase
import com.solutionium.shared.domain.user.GetCurrentUserUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.paging.cachedIn

data class ReviewFormState(
    val rating: Int = 0,
    val reviewText: String = "",
    val criteriaRatings: Map<String, Int> = emptyMap(),
    val isSubmitting: Boolean = false,
    val checkingLogin: Boolean = false,
    val loadingUser: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userDetails: UserDetails? = null,
)

class ReviewViewModel(
    initialArgs: Map<String, String> = emptyMap(),
    getReviewListPaging: GetReviewListPagingUseCase,
    private val submitReviewUseCase: SubmitReviewUseCase,
    private val checkLoginUserUseCase: CheckLoginUserUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val reviewCriteriaUseCase: ReviewCriteriaUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val productId: Int =
        initialArgs["productId"]?.toIntOrNull()
            ?: throw IllegalArgumentException("Product ID is required")

    private val categoryIds: List<Int> =
        initialArgs["categoryIds"]
            ?.split(",")
            ?.mapNotNull { it.toIntOrNull() }
            ?: emptyList()

    val reviews = getReviewListPaging(
        listOf(
            FilterCriterion("product", productId.toString()),
        ),
    ).cachedIn(scope)

    private val _showReviewDialog = MutableStateFlow(false)
    val showReviewDialog: StateFlow<Boolean> = _showReviewDialog.asStateFlow()

    private val _state = MutableStateFlow(ReviewFormState())
    val state: StateFlow<ReviewFormState> = _state.asStateFlow()

    private val _productReviewCriteria = MutableStateFlow<List<String>>(emptyList())
    val productReviewCriteria: StateFlow<List<String>> = _productReviewCriteria.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        scope.launch {
            _state.update { it.copy(checkingLogin = true) }
            val isLoggedIn = checkLoginUserUseCase().first()
            if (isLoggedIn) {
                _state.update { it.copy(isLoggedIn = true, checkingLogin = false) }
                fetchUserDetails()
            } else {
                _state.update { it.copy(isLoggedIn = false, checkingLogin = false) }
            }
        }
    }

    private fun fetchUserDetails() {
        scope.launch {
            _state.update { it.copy(loadingUser = true) }
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update { it.copy(userDetails = result.data, loadingUser = false) }
                    }

                    is Result.Failure -> {
                        _state.update { it.copy(loadingUser = false) }
                        when (result.error) {
                            is GeneralError.ApiError -> Unit
                            GeneralError.NetworkError -> Unit
                            is GeneralError.UnknownError -> Unit
                        }
                    }
                }
            }
        }
    }

    fun onOpenReviewDialog() {
        if (_state.value.isLoggedIn) {
            loadReviewCriteria()
            _showReviewDialog.value = true
        }
    }

    fun onCloseReviewDialog() {
        _showReviewDialog.value = false
    }

    fun onRatingChange(newRating: Int) {
        _state.update { it.copy(rating = newRating) }
    }

    fun onReviewTextChange(newText: String) {
        _state.update { it.copy(reviewText = newText) }
    }

    fun onCriteriaRatingChange(criteriaLabel: String, rating: Int) {
        val updatedCriteria = _state.value.criteriaRatings.toMutableMap()
        updatedCriteria[criteriaLabel] = rating
        _state.update { it.copy(criteriaRatings = updatedCriteria) }
    }

    private fun loadReviewCriteria() {
        scope.launch {
            _productReviewCriteria.value =
                if (categoryIds.isNotEmpty()) reviewCriteriaUseCase(categoryIds) else emptyList()
        }
    }

    fun submitReview() {
        scope.launch {
            _state.update { it.copy(isSubmitting = true) }
            val reviewFormState = _state.value
            val newReview = NewReview(
                productID = productId,
                reviewer = reviewFormState.userDetails?.displayName ?: "",
                reviewerEmail = reviewFormState.userDetails?.email ?: "app@noemail.io",
                review = reviewFormState.reviewText,
                rating = reviewFormState.rating,
                criteriaRatings = reviewFormState.criteriaRatings.map { (label, value) ->
                    CriteriaRating(label, value)
                },
            )

            when (submitReviewUseCase(newReview)) {
                is Result.Success -> {
                    _state.update { it.copy(isSubmitting = false) }
                    onCloseReviewDialog()
                }

                is Result.Failure -> {
                    _state.update { it.copy(isSubmitting = false) }
                }
            }
        }
    }

    fun clear() {
        scope.cancel()
    }
}
