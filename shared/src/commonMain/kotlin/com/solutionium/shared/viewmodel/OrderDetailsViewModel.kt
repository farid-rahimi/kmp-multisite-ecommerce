package com.solutionium.shared.viewmodel

import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.domain.order.GetOrderByIdUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrderDetailsViewModel(
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
    args: Map<String, String> = emptyMap(),
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val orderId = args["order_id"]?.toIntOrNull()

    private val _state = MutableStateFlow(OrderDetailsUiState())
    val state = _state.asStateFlow()

    init {
        loadOrder()
    }

    fun retry() {
        loadOrder()
    }

    private fun loadOrder() {
        val safeOrderId = orderId
        if (safeOrderId == null) {
            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Invalid order id",
                )
            }
            return
        }

        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getOrderByIdUseCase(safeOrderId)) {
                is com.solutionium.shared.data.model.Result.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            order = result.data,
                            errorMessage = null,
                        )
                    }
                }

                is com.solutionium.shared.data.model.Result.Failure -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            order = null,
                            errorMessage = result.error.toReadableMessage(),
                        )
                    }
                }
            }
        }
    }

    fun clear() {
        scope.cancel()
    }
}

private fun GeneralError.toReadableMessage(): String = when (this) {
    is GeneralError.ApiError -> message ?: "Could not load order details."
    is GeneralError.NetworkError -> "Network error. Please check your connection."
    is GeneralError.UnknownError -> error.message ?: "Could not load order details."
}
