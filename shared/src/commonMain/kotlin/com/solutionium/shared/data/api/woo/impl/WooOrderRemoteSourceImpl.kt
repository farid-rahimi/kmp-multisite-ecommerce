package com.solutionium.shared.data.api.woo.impl

import com.solutionium.shared.data.api.woo.converters.toModel
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Order
import com.solutionium.shared.data.api.woo.WooOrderRemoteSource
import com.solutionium.shared.data.api.woo.handleNetworkResponse
import com.solutionium.shared.data.network.clients.WooOrderClient
import com.solutionium.shared.data.network.NetworkConfigProvider
import com.solutionium.shared.data.model.Result


class WooOrderRemoteSourceImpl(
    private val wooOrderService: WooOrderClient,
    private val networkConfigProvider: NetworkConfigProvider,
): WooOrderRemoteSource {

    override suspend fun getOrderList(
        page: Int,
        queries: Map<String, String>
    ): Result<List<Order>, GeneralError> =
        handleNetworkResponse(
            networkCall = { wooOrderService.getOrders(page, queries) },
            mapper = { responseList ->
                responseList.map { it.toModel(networkConfigProvider.get().baseUrl) }
            }
        )

    override suspend fun getOrderById(orderId: Int): Result<Order, GeneralError> =
        handleNetworkResponse(
            networkCall = { wooOrderService.getOrderById(orderId) },
            mapper = { response -> response.toModel(networkConfigProvider.get().baseUrl) }
        )

}
