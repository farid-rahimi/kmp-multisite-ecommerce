package com.solutionium.shared.domain.order

import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Order
import com.solutionium.shared.data.model.Result

interface GetOrderByIdUseCase {
    suspend operator fun invoke(orderId: Int): Result<Order, GeneralError>
}
