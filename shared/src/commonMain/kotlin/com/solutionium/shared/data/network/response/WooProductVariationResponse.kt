package com.solutionium.shared.data.network.response


import com.solutionium.shared.data.network.common.MetaDatum
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

typealias WooProductVariationListResponse = List<WooProductVariationResponse>


@Serializable
data class WooProductVariationResponse(
    val id: Int,
    val type: String,

    @SerialName("date_created")
    val dateCreated: String,

    @SerialName("date_created_gmt")
    val dateCreatedGmt: String,

    @SerialName("date_modified")
    val dateModified: String,

    @SerialName("date_modified_gmt")
    val dateModifiedGmt: String,

    val description: String,
    val permalink: String,
    val sku: String,

    @SerialName("global_unique_id")
    val globalUniqueID: String,

    val price: String,

    @SerialName("regular_price")
    val regularPrice: String? = null,

    @SerialName("sale_price")
    val salePrice: String? = null,

    @SerialName("date_on_sale_from")
    val dateOnSaleFrom: String? = null,

    @SerialName("date_on_sale_from_gmt")
    val dateOnSaleFromGmt: String? = null,

    @SerialName("date_on_sale_to")
    val dateOnSaleTo: String? = null,

    @SerialName("date_on_sale_to_gmt")
    val dateOnSaleToGmt: String? = null,

    @SerialName("on_sale")
    val onSale: Boolean,

    val status: String,
    val purchasable: Boolean,
    val virtual: Boolean,

    @SerialName("tax_status")
    val taxStatus: String,

    @SerialName("tax_class")
    val taxClass: String,

    @SerialName("manage_stock")
    @Serializable(with = FlexibleManageStockSerializer::class)
    val manageStock: Boolean,

    @SerialName("stock_quantity")
    val stockQuantity: Int? = null,

    @SerialName("stock_status")
    val stockStatus: String,

    val backorders: String,

    @SerialName("backorders_allowed")
    val backordersAllowed: Boolean,

    val backordered: Boolean,

    @SerialName("low_stock_amount")
    val lowStockAmount: Int? = null,

    val weight: String,
    val dimensions: Dimensions,

    @SerialName("shipping_class")
    val shippingClass: String,

    @SerialName("shipping_class_id")
    val shippingClassID: Long,

    val image: Image,

    val attributes: List<WooVariationAttribute>,

    @SerialName("menu_order")
    val menuOrder: Long,

    @SerialName("meta_data")
    val metaData: List<MetaDatum>,

    val name: String,

    @SerialName("parent_id")
    val parentID: Long,
)

/**
 * Woo variations may return `manage_stock` as boolean or string ("parent").
 * Treat "parent" as true so stock-aware UI remains enabled for inherited behavior.
 */
object FlexibleManageStockSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleManageStock", PrimitiveKind.BOOLEAN)

    override fun deserialize(decoder: Decoder): Boolean {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeBoolean()

        val primitive: JsonPrimitive = jsonDecoder.decodeJsonElement().jsonPrimitive
        primitive.booleanOrNull?.let { return it }
        primitive.intOrNull?.let { return it != 0 }

        return when (primitive.content.trim().lowercase()) {
            "true", "1", "yes" -> true
            "parent" -> false
            else -> false
        }
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeBoolean(value)
    }
}

@Serializable
data class WooVariationAttribute(
    val id: Int = 0,
    val name: String, // "Color"
    val slug: String? = null, // "pa_color"
    val option: String // "Red"
)

