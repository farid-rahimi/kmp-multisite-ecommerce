package com.solutionium.shared.domain.order

import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Order
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.orders.OrderRepository

class GetOrderByIdUseCaseImpl(
    private val orderRepository: OrderRepository,
) : GetOrderByIdUseCase {
    override suspend fun invoke(orderId: Int): Result<Order, GeneralError> =
        orderRepository.getOrderById(orderId)
}
