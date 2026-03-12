package com.solutionium.shared.data.network.response

import kotlinx.serialization.*
import kotlinx.serialization.json.JsonElement

typealias PaymentGatewayListResponse = List<PaymentGatewayResponse>

@Serializable
data class PaymentGatewayResponse (
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,

    //@Serializable(with = IntOrStringSerializer::class)
    //var order: Int?,
    val enabled: Boolean? = null,

    @SerialName("method_title")
    val methodTitle: String? = null,

    @SerialName("method_description")
    val methodDescription: String? = null,

    @SerialName("method_supports")
    val methodSupports: List<String>? = null,

    val settings: JsonElement? = null,

    )

