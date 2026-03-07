package com.solutionium.shared.data.api.woo.impl

import com.solutionium.shared.data.api.woo.converters.toModel
import com.solutionium.shared.data.model.AppConfig
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.api.woo.WooConfigRemoteSource
import com.solutionium.shared.data.api.woo.handleNetworkResponse
import com.solutionium.shared.data.network.clients.UserClient
import com.solutionium.shared.data.model.Result


internal class WooConfigRemoteSourceImpl(
    private val userService: UserClient,
) : WooConfigRemoteSource {
    override suspend fun getAppConfig(languageCode: String?): Result<AppConfig, GeneralError> =
        handleNetworkResponse(
            networkCall = { userService.getAppConfig() },
            mapper = { it.toModel(languageCode) }
        )

    override suspend fun getPrivacyPolicy(): Result<String, GeneralError> =
        handleNetworkResponse(
            networkCall = { userService.getPrivacyPolicy() },
            mapper = { it.message ?: "" }
        )

}
