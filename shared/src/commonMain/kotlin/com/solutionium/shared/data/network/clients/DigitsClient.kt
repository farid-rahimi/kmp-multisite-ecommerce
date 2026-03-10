package com.solutionium.shared.data.network.clients

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
    private val passwordRegisterPath: String = "wp-json/digits/v1/register_user",
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

    suspend fun registerUser(
        name: String,
        email: String,
        phone: String,
        password: String,
    ) = run {
        val candidates = listOf(
            passwordRegisterPath,
            "wp-json/woo-mobile-auth/v1/register_user",
            "wp-json/digits/v1/register_user",
        ).distinct()

        var lastResult: com.solutionium.shared.data.network.adapter.NetworkResponse<DigitsLoginRegisterResponse, DigitsErrorResponse>? =
            null

        for (candidate in candidates) {
            val result = registerUserViaPath(
                pathValue = candidate,
                name = name,
                email = email,
                phone = phone,
                password = password,
            )
            lastResult = result

            when (result) {
                is com.solutionium.shared.data.network.adapter.NetworkResponse.Success -> return@run result
                is com.solutionium.shared.data.network.adapter.NetworkResponse.ApiError -> {
                    if (result.code == 404 || result.code == 405) continue
                    return@run result
                }

                is com.solutionium.shared.data.network.adapter.NetworkResponse.NetworkError,
                is com.solutionium.shared.data.network.adapter.NetworkResponse.UnknownError -> return@run result
            }
        }

        lastResult ?: registerUserViaPath(
            pathValue = "wp-json/woo-mobile-auth/v1/register_user",
            name = name,
            email = email,
            phone = phone,
            password = password,
        )
    }

    private suspend fun registerUserViaPath(
        pathValue: String,
        name: String,
        email: String,
        phone: String,
        password: String,
    ) = client.safeRequest<DigitsLoginRegisterResponse, DigitsErrorResponse> {
        method = HttpMethod.Post
        url { path(pathValue) }
        setBody(
            MultiPartFormDataContent(
                formData {
                    append("name", name)
                    append("email", email)
                    append("phone", phone)
                    append("password", password)
                },
            ),
        )
    }

    suspend fun oneClick(params: Map<String, String>) =
        client.safeRequest<DigitsLoginRegisterResponse, DigitsErrorResponse> {
            method = HttpMethod.Post
            url {
                path("wp-json/digits/v1/one_click")
                params.forEach { (key, value) -> parameter(key, value) }
            }
        }

    suspend fun requestPasswordResetOtp(email: String) =
        client.safeRequest<DigitsSimpleResponse, DigitsErrorResponse> {
            method = HttpMethod.Post
            url { path("wp-json/woo-mobile-auth/v1/request_password_otp") }
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("email", email)
                    },
                ),
            )
        }

    suspend fun verifyPasswordResetOtp(email: String, otp: String) =
        client.safeRequest<DigitsSimpleResponse, DigitsErrorResponse> {
            method = HttpMethod.Post
            url { path("wp-json/woo-mobile-auth/v1/verify_password_otp") }
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("email", email)
                        append("otp", otp)
                    },
                ),
            )
        }

    suspend fun resetPasswordByOtp(email: String, otp: String, newPassword: String) =
        client.safeRequest<DigitsSimpleResponse, DigitsErrorResponse> {
            method = HttpMethod.Post
            url { path("wp-json/woo-mobile-auth/v1/reset_password_with_otp") }
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("email", email)
                        append("otp", otp)
                        append("new_password", newPassword)
                    },
                ),
            )
        }

    suspend fun logout(token: String) =
        client.safeRequest<DigitsSimpleResponse, DigitsSimpleResponse> {
            method = HttpMethod.Post
            url { path("wp-json/digits/v1/logout") }
            header(HttpHeaders.Authorization, token)
        }
}
