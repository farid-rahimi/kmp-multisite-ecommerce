package com.solutionium.shared.data.api.woo

import com.solutionium.shared.data.model.AppConfig
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.Result


interface WooConfigRemoteSource {

    suspend fun getAppConfig(languageCode: String? = null): Result<AppConfig, GeneralError>

    suspend fun getPrivacyPolicy(): Result<String, GeneralError>
}
