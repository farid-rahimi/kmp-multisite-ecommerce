package com.solutionium.shared.data.checkout

import com.solutionium.shared.data.api.woo.WooCheckoutRemoteSource
import com.solutionium.shared.data.local.TokenStore
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.NewOrderData
import com.solutionium.shared.data.model.Order
import com.solutionium.shared.data.model.PaymentGateway
import com.solutionium.shared.data.model.Result
import com.solutionium.shared.data.model.ShippingMethod

class CheckoutRepositoryImpl(
    private val checkoutRemoteSource: WooCheckoutRemoteSource,
    private val tokenStore: TokenStore,
) : CheckoutRepository {
    override suspend fun getPaymentGateways(forcedEnabled: List<String>): Result<List<PaymentGateway>, GeneralError> =
        checkoutRemoteSource.getPaymentGateways(forcedEnabled)

    override suspend fun getShippingMethods(): Result<List<ShippingMethod>, GeneralError> =
        checkoutRemoteSource.getShippingMethods()

    override suspend fun createOrder(orderData: NewOrderData): Result<Order, GeneralError> {
        val createOrderResult =
            checkoutRemoteSource.createOrder(orderData.copy(customerID = tokenStore.getUserId()?.toLong() ?: 0L))

        if (createOrderResult !is Result.Success) {
            return createOrderResult
        }

        val createdOrder = createOrderResult.data
        val token = tokenStore.getToken()?.trim().orEmpty()
        val orderKey = createdOrder.orderKey?.trim().orEmpty()

        if (orderKey.isBlank()) {
            return createOrderResult
        }

        val paymentSessionResult = checkoutRemoteSource.createPaymentSessionUrl(
            orderId = createdOrder.id,
            orderKey = orderKey,
            bearerToken = token.takeIf { it.isNotBlank() }?.let { "Bearer $it" },
        )

        return when (paymentSessionResult) {
            is Result.Success -> {
                val sessionUrl = paymentSessionResult.data.trim()
                if (sessionUrl.isNotBlank()) {
                    println("CheckoutRepository: payment_session_url success for order=${createdOrder.id}")
                    Result.Success(createdOrder.copy(paymentUrl = sessionUrl))
                } else {
                    println("CheckoutRepository: payment_session_url returned blank for order=${createdOrder.id}, using fallback paymentUrl")
                    createOrderResult
                }
            }

            is Result.Failure -> {
                println("CheckoutRepository: payment_session_url failed for order=${createdOrder.id}, using fallback paymentUrl, error=${paymentSessionResult.error}")
                createOrderResult
            }
        }
    }



}
