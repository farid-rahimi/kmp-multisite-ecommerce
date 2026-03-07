package com.solutionium.shared.data.network.common

import com.solutionium.shared.data.network.request.OrderMetadata
import com.solutionium.shared.data.network.response.SimpleTaxLine
import com.solutionium.shared.data.network.response.TaxLine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WooFeeLine (
    val id: Long? = null, // id can be null when creating an order
    val name: String,
    val total: String, // after discounts

    @SerialName("tax_class")
    val taxClass: String? = null,

    @SerialName("tax_status")
    val taxStatus: String? = "none", // none or taxable

    @SerialName("total_tax")
    val totalTax: String? = null, // Read only, after discounts

    val taxes: List<SimpleTaxLine>? = null, // Read only

    @SerialName("meta_data")
    val metaData: List<OrderMetadata>? = null
)