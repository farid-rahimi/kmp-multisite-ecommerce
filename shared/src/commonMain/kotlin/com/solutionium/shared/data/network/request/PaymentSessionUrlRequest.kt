package com.solutionium.shared.data.network.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentSessionUrlRequest(
    @SerialName("order_id")
    val orderId: Int,
    @SerialName("order_key")
    val orderKey: String,
    @SerialName("app_scheme")
    val appScheme: String,
)
