package com.solutionium.shared.data.api.woo

import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.NewOrderData
import com.solutionium.shared.data.model.Order
import com.solutionium.shared.data.model.PaymentGateway
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.model.ShippingMethod

interface WooCheckoutRemoteSource {
    suspend fun getPaymentGateways(forcedEnabled: List<String> = emptyList()): Result<List<PaymentGateway>, GeneralError>

    suspend fun getShippingMethods(): Result<List<ShippingMethod>, GeneralError>

    suspend fun createOrder(orderData: NewOrderData): Result<Order, GeneralError>

    suspend fun createPaymentSessionUrl(
        orderId: Int,
        orderKey: String,
        bearerToken: String?,
    ): Result<String, GeneralError>
}
