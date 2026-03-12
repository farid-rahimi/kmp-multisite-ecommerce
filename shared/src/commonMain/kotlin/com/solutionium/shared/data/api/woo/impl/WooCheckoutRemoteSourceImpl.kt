package com.solutionium.shared.data.api.woo.impl

import com.solutionium.shared.data.api.woo.converters.toModel
import com.solutionium.shared.data.api.woo.converters.toRequestBody
import com.solutionium.shared.data.api.woo.converters.toShippingMethod
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.NewOrderData
import com.solutionium.shared.data.model.Order
import com.solutionium.shared.data.model.PaymentGateway
import com.solutionium.shared.data.model.ShippingMethod
import com.solutionium.shared.data.api.woo.WooCheckoutRemoteSource
import com.solutionium.shared.data.api.woo.converters.toPaymentGateway
import com.solutionium.shared.data.api.woo.handleNetworkResponse
import com.solutionium.shared.data.network.clients.WooCheckoutOrderClient
import com.solutionium.shared.data.network.clients.UserClient
import com.solutionium.shared.data.network.NetworkConfigProvider
import com.solutionium.shared.data.network.response.PaymentGatewayResponse
import com.solutionium.shared.data.model.Result
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull


internal class WooCheckoutRemoteSourceImpl(
    //private val wooCheckoutOrderService: WooCheckoutOrderService,
    private val checkoutOrderApi: WooCheckoutOrderClient,
    private val networkConfigProvider: NetworkConfigProvider,
    private val userClient: UserClient,
) : WooCheckoutRemoteSource {
    override suspend fun getPaymentGateways(forcedEnabled: List<String>): Result<List<PaymentGateway>, GeneralError> =
        handleNetworkResponse(
            networkCall = { checkoutOrderApi.getPaymentGateways() },
            mapper = { responseList ->
                val forced = forcedEnabled
                    .flatMap { value -> value.split(",") }
                    .map { it.trim().lowercase() }
                    .filter { it.isNotBlank() }
                    .toSet()

                responseList.filter { gateway ->
                    val gatewayId = gateway.id?.trim().orEmpty()
                    val isEnabledByBoolean = gateway.enabled == true
                    val isEnabledBySettings = isGatewayEnabledFromSettings(gateway)
                    val isForced = gatewayId.lowercase() in forced

                    isEnabledByBoolean || isEnabledBySettings || isForced
                }
                    .map { it.toPaymentGateway() }
            }
        )


    override suspend fun getShippingMethods(): Result<List<ShippingMethod>, GeneralError> =
        handleNetworkResponse(
            networkCall = { checkoutOrderApi.getShippingMethods() },
            mapper = { responseList ->
                responseList.filter { it.enabled == true }.map { it.toShippingMethod() }
            }
        )


    override suspend fun createOrder(orderData: NewOrderData): Result<Order, GeneralError> =
        handleNetworkResponse(
            networkCall = { checkoutOrderApi.createOrder(orderData.toRequestBody()) },
            mapper = { response -> response.toModel(networkConfigProvider.get().baseUrl) }
        )

    override suspend fun createPaymentSessionUrl(
        orderId: Int,
        orderKey: String,
        bearerToken: String?,
    ): Result<String, GeneralError> =
        handleNetworkResponse(
            networkCall = {
                userClient.createPaymentSessionUrl(
                    orderId = orderId,
                    orderKey = orderKey,
                    appScheme = networkConfigProvider.get().paymentReturnScheme,
                    token = bearerToken,
                )
            },
            mapper = { response -> response.data?.paymentUrl.orEmpty() }
        )



}

private fun isGatewayEnabledFromSettings(gateway: PaymentGatewayResponse): Boolean {
    return extractEnabledSettingValue(gateway.settings)
        ?.trim()
        ?.equals("yes", ignoreCase = true) == true
}

private fun extractEnabledSettingValue(settings: JsonElement?): String? {
    return when (settings) {
        null -> null
        is JsonObject -> {
            // Case A: settings is an object map and contains an "enabled" node.
            // enabled can be either direct primitive or object with nested "value".
            val enabledNode = settings["enabled"] ?: return null
            when (enabledNode) {
                is JsonPrimitive -> enabledNode.contentOrNull
                is JsonObject -> (enabledNode["value"] as? JsonPrimitive)?.contentOrNull
                else -> null
            }
        }
        is JsonArray -> {
            // Case B: settings is an array of setting objects with "id":"enabled".
            settings
                .firstNotNullOfOrNull { item ->
                    val obj = item as? JsonObject ?: return@firstNotNullOfOrNull null
                    val id = (obj["id"] as? JsonPrimitive)?.contentOrNull ?: return@firstNotNullOfOrNull null
                    if (!id.equals("enabled", ignoreCase = true)) return@firstNotNullOfOrNull null
                    (obj["value"] as? JsonPrimitive)?.contentOrNull
                }
        }
        else -> null
    }
}
