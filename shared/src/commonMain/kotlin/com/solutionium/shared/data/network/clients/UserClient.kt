package com.solutionium.shared.data.network.clients

import com.solutionium.shared.data.network.request.EditUserRequest
import com.solutionium.shared.data.network.request.PaymentSessionUrlRequest
import com.solutionium.shared.data.network.response.AppConfigResponse
import com.solutionium.shared.data.network.response.FavoritesResponse
import com.solutionium.shared.data.network.response.PaymentSessionUrlResponse
import com.solutionium.shared.data.network.response.PrivacyPolicyResponse
import com.solutionium.shared.data.network.response.WooErrorResponse
import com.solutionium.shared.data.network.response.WooUserWalletResponse
import com.solutionium.shared.data.network.response.WooWalletConfigResponse
import com.solutionium.shared.data.network.response.WpUserResponse
import com.solutionium.shared.data.network.safeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
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

    suspend fun updateUser(userData: EditUserRequest, token: String) =
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
            url { path("app/config-test.php") }
            header(HttpHeaders.CacheControl, "no-cache")
        }

    suspend fun createPaymentSessionUrl(
        orderId: Int,
        orderKey: String,
        appScheme: String,
        token: String?,
    ) =
        client.safeRequest<PaymentSessionUrlResponse, WooErrorResponse> {
            method = HttpMethod.Post
            url { path("wp-json/woo-mobile-auth/v1/payment_session_url") }
            setBody(
                PaymentSessionUrlRequest(
                    orderId = orderId,
                    orderKey = orderKey,
                    appScheme = appScheme,
                ),
            )
            if (!token.isNullOrBlank()) {
                header(HttpHeaders.Authorization, token)
            }
        }

    suspend fun getPrivacyPolicy() =
        client.safeRequest<PrivacyPolicyResponse, WooErrorResponse> {
            method = HttpMethod.Get
            url { path("app/privacy-policy.php") }
            header(HttpHeaders.CacheControl, "no-cache")
        }

    suspend fun getFavorites(token: String) =
        client.safeRequest<FavoritesResponse, WooErrorResponse> {
            method = HttpMethod.Get
            url { path("wp-json/woo-mobile-auth/v1/favorites") }
            header(HttpHeaders.Authorization, token)
        }

    suspend fun toggleFavorite(
        token: String,
        productId: Int,
        isFavorite: Boolean,
    ) =
        client.safeRequest<FavoritesResponse, WooErrorResponse> {
            method = HttpMethod.Post
            url { path("wp-json/woo-mobile-auth/v1/favorites/toggle") }
            header(HttpHeaders.Authorization, token)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("product_id", productId.toString())
                        append("is_favorite", if (isFavorite) "1" else "0")
                    },
                ),
            )
        }

    suspend fun setFavorites(token: String, ids: Set<Int>) =
        client.safeRequest<FavoritesResponse, WooErrorResponse> {
            method = HttpMethod.Post
            url { path("wp-json/woo-mobile-auth/v1/favorites/set") }
            header(HttpHeaders.Authorization, token)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("ids", ids.sorted().joinToString(","))
                    },
                ),
            )
        }
}
