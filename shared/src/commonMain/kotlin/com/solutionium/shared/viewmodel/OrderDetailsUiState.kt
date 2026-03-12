package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.Order

data class OrderDetailsUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val errorMessage: String? = null,
)
