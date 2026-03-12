package com.solutionium.shared.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentSessionUrlResponse(
    val success: Boolean = false,
    val data: PaymentSessionUrlData? = null,
)

@Serializable
data class PaymentSessionUrlData(
    @SerialName("payment_url")
    val paymentUrl: String? = null,
    @SerialName("order_id")
    val orderId: Int? = null,
    @SerialName("expires_in")
    val expiresIn: Int? = null,
)
