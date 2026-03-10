package com.solutionium.shared.data.network.clients

import com.solutionium.shared.data.network.request.EditUserRequest
import com.solutionium.shared.data.network.response.AppConfigResponse
import com.solutionium.shared.data.network.response.PrivacyPolicyResponse
import com.solutionium.shared.data.network.response.WooErrorResponse
import com.solutionium.shared.data.network.response.WooUserWalletResponse
import com.solutionium.shared.data.network.response.WooWalletConfigResponse
import com.solutionium.shared.data.network.response.WpUserResponse
import com.solutionium.shared.data.network.safeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.path

class UserClient(
    private val client: HttpClient
) {

    suspend fun getMe(token: String) =
        client.safeRequest<WpUserResponse, WooErrorResponse> {
            method = HttpMethod.Get
            url { path("wp-json/wp/v2/users/me") }
            header(HttpHeaders.Authorization, token)
        }

    suspend fun updateUser(userId: String, userData: EditUserRequest, token: String) =
        client.safeRequest<WpUserResponse, WooErrorResponse> {
            // Profile update is handled by custom mobile-auth plugin endpoint.
            // userId is intentionally ignored because endpoint uses authenticated user.
            method = HttpMethod.Post
            url { path("wp-json/woo-mobile-auth/v1/update_profile") }
            setBody(userData)
            header(HttpHeaders.Authorization, token)
        }

    suspend fun getUserWallet(token: String) =
        client.safeRequest<WooUserWalletResponse, WooErrorResponse> {
            method = HttpMethod.Get
            url { path("wp-json/wallet/v1/user") }
            header(HttpHeaders.Authorization, token)
        }

    suspend fun getWalletConfig() =
        client.safeRequest<WooWalletConfigResponse, WooErrorResponse> {
            method = HttpMethod.Get
            url { path("wp-json/wallet/v1/settings") }
        }

    suspend fun getAppConfig() =
        client.safeRequest<AppConfigResponse, WooErrorResponse> {
            method = HttpMethod.Get
            url { path("app/config.php") }
            header(HttpHeaders.CacheControl, "no-cache")
        }

    suspend fun getPrivacyPolicy() =
        client.safeRequest<PrivacyPolicyResponse, WooErrorResponse> {
            method = HttpMethod.Get
            url { path("app/privacy-policy.php") }
            header(HttpHeaders.CacheControl, "no-cache")
        }
}
