package com.solutionium.shared.data.network.response

import com.solutionium.shared.data.network.common.WooAddress
import com.solutionium.shared.data.network.common.WooFeeLine
import com.solutionium.shared.data.network.common.MetaDatum
import kotlinx.serialization.*
import kotlinx.serialization.json.*

typealias WooOrderListResponse = List<WooOrderResponse>


@Serializable
data class WooOrderResponse (
    val id: Int,

    @SerialName("parent_id")
    val parentID: Int,

    val number: String,

    @SerialName("order_key")
    val orderKey: String,

    @SerialName("created_via")
    val createdVia: String?,

    val version: String,
    val status: String,
    val currency: String,

    @SerialName("date_created")
    val dateCreated: String,

    @SerialName("date_created_gmt")
    val dateCreatedGmt: String,

    @SerialName("date_modified")
    val dateModified: String,

    @SerialName("date_modified_gmt")
    val dateModifiedGmt: String,

    @SerialName("discount_total")
    val discountTotal: String,

    @SerialName("discount_tax")
    val discountTax: String,

    @SerialName("shipping_total")
    val shippingTotal: String,

    @SerialName("shipping_tax")
    val shippingTax: String,

    @SerialName("cart_tax")
    val cartTax: String,

    val total: String,

    @SerialName("total_tax")
    val totalTax: String,

    @SerialName("prices_include_tax")
    val pricesIncludeTax: Boolean,

    @SerialName("customer_id")
    val customerID: Long,

    @SerialName("customer_ip_address")
    val customerIPAddress: String,

    @SerialName("customer_user_agent")
    val customerUserAgent: String,

    @SerialName("customer_note")
    val customerNote: String,

    val billing: WooAddress,
    val shipping: WooAddress,

    @SerialName("payment_method")
    val paymentMethod: String,

    @SerialName("payment_method_title")
    val paymentMethodTitle: String,

    @SerialName("transaction_id")
    val transactionID: String,

    @SerialName("date_paid")
    val datePaid: String? = null,

    @SerialName("date_paid_gmt")
    val datePaidGmt: String? = null,

    @SerialName("date_completed")
    val dateCompleted: String? = null,

    @SerialName("date_completed_gmt")
    val dateCompletedGmt: String? = null,

    @SerialName("cart_hash")
    val cartHash: String,

    @SerialName("meta_data")
    val metaData: List<MetaDatum>? = null,

    @SerialName("line_items")
    val lineItems: List<WooLineItem>,

    @SerialName("tax_lines")
    val taxLines: List<TaxLine>? = null,

    @SerialName("shipping_lines")
    val shippingLines: List<ShippingLine>,

    @SerialName("fee_lines")
    val feeLines: List<WooFeeLine>?,

    @SerialName("coupon_lines")
    val couponLines: JsonArray? = null,

    val refunds: List<Refund>?,

    )

@Serializable
data class WooLineItem (
    val id: Int,
    val name: String,

    @SerialName("product_id")
    val productID: Int,

    @SerialName("variation_id")
    val variationID: Int,

    val quantity: Int,

    @SerialName("tax_class")
    val taxClass: String,

    val subtotal: String,

    @SerialName("subtotal_tax")
    val subtotalTax: String,

    val total: String,

    @SerialName("total_tax")
    val totalTax: String,

    val taxes: List<Tax>,

    @SerialName("meta_data")
    val metaData: List<MetaDatum>,

    val sku: String,
    val price: Double,
    val image: ImageItem?
)

@Serializable
data class ImageItem (
    val id: String?,
    val src: String?
)

@Serializable
data class Tax (
    val id: Long,
    val total: String,
    val subtotal: String
)


@Serializable
data class ShippingLine (
    val id: Long,

    @SerialName("method_title")
    val methodTitle: String,

    @SerialName("method_id")
    val methodID: String,

    val total: String,

    @SerialName("total_tax")
    val totalTax: String,

    val taxes: List<SimpleTaxLine>?,

    @SerialName("meta_data")
    val metaData: List<MetaDatum>?
)



@Serializable
data class TaxLine (
    val id: Long,

    @SerialName("rate_code")
    val rateCode: String,

    @SerialName("rate_id")
    val rateID: Long,

    val label: String,
    val compound: Boolean,

    @SerialName("tax_total")
    val taxTotal: String,

    @SerialName("shipping_tax_total")
    val shippingTaxTotal: String,

    @SerialName("meta_data")
    val metaData: List<MetaDatum>?
)

@Serializable
data class SimpleTaxLine (
    val id: Int,
    val total: String?,
    val subtotal: String?
)


@Serializable
data class Refund (
    val id: Long,
    val reason: String,
    val total: String
)



