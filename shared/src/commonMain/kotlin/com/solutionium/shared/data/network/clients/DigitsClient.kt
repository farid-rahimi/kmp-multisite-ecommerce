package com.solutionium.shared.data.network.clients

import com.solutionium.shared.data.network.request.DigitsRegisterRequest
import com.solutionium.shared.data.network.response.DigitsErrorResponse
import com.solutionium.shared.data.network.response.DigitsLoginRegisterResponse
import com.solutionium.shared.data.network.response.DigitsOtpErrorResponse
import com.solutionium.shared.data.network.response.DigitsOtpResponse
import com.solutionium.shared.data.network.response.DigitsSimpleResponse
import com.solutionium.shared.data.network.safeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.path

class DigitsClient(
    private val client: HttpClient,
    private val passwordLoginPath: String = "wp-json/digits/v1/login_user",
) {

    suspend fun sendOTP(params: Map<String, String>) =
        client.safeRequest<DigitsOtpResponse, DigitsOtpErrorResponse> {
            method = HttpMethod.Post
            url {
                path("wp-json/digits/v1/send_otp")
                params.forEach { (key, value) -> parameter(key, value) }
            }
        }

    suspend fun loginUser(user: String, password: String) =
        run {
            val candidates = listOf(
                passwordLoginPath,
                "wp-json/digits/v1/login_user",
                "wp-json/woo-mobile-auth/v1/login_user",
            ).distinct()

            var lastResult: com.solutionium.shared.data.network.adapter.NetworkResponse<DigitsLoginRegisterResponse, DigitsErrorResponse>? =
                null

            for (candidate in candidates) {
                val result = loginUserViaPath(candidate, user, password)
                lastResult = result

                when (result) {
                    is com.solutionium.shared.data.network.adapter.NetworkResponse.Success -> return@run result
                    is com.solutionium.shared.data.network.adapter.NetworkResponse.ApiError -> {
                        // If endpoint is missing on this site, try next candidate.
                        if (result.code == 404 || result.code == 405) continue
                        return@run result
                    }

                    is com.solutionium.shared.data.network.adapter.NetworkResponse.NetworkError,
                    is com.solutionium.shared.data.network.adapter.NetworkResponse.UnknownError -> return@run result
                }
            }

            lastResult ?: loginUserViaPath("wp-json/digits/v1/login_user", user, password)
        }

    private suspend fun loginUserViaPath(
        pathValue: String,
        user: String,
        password: String,
    ) = client.safeRequest<DigitsLoginRegisterResponse, DigitsErrorResponse> {
        method = HttpMethod.Post
        url { path(pathValue) }
        setBody(
            MultiPartFormDataContent(
                formData {
                    append("user", user)
                    append("password", password)
                },
            ),
        )
    }

    suspend fun verifyOTP(otp: String, phone: String) =
        client.safeRequest<DigitsLoginRegisterResponse, DigitsErrorResponse> {
            method = HttpMethod.Post
            url {
                path("wp-json/digits/v1/verify_otp")
                parameter("otp", otp)
                parameter("phone", phone)
            }
        }

    suspend fun resendOTP(phone: String) =
        client.safeRequest<DigitsLoginRegisterResponse, DigitsErrorResponse> {
            method = HttpMethod.Post
            url {
                path("wp-json/digits/v1/resend_otp")
                parameter("phone", phone)
            }
        }

    suspend fun registerUser(body: DigitsRegisterRequest) =
        client.safeRequest<DigitsLoginRegisterResponse, DigitsErrorResponse> {
            method = HttpMethod.Post
            url { path("wp-json/digits/v1/register_user") }
            setBody(body)
        }

    suspend fun oneClick(params: Map<String, String>) =
        client.safeRequest<DigitsLoginRegisterResponse, DigitsErrorResponse> {
            method = HttpMethod.Post
            url {
                path("wp-json/digits/v1/one_click")
                params.forEach { (key, value) -> parameter(key, value) }
            }
        }

    suspend fun logout(token: String) =
        client.safeRequest<DigitsSimpleResponse, DigitsSimpleResponse> {
            method = HttpMethod.Post
            url { path("wp-json/digits/v1/logout") }
            header(HttpHeaders.Authorization, token)
        }
}
