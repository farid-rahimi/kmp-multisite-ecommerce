package com.solutionium.shared.data.network

import com.solutionium.shared.data.network.adapter.NetworkResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

/**
 * Platform-neutral safe request wrapper for KMP.
 */
suspend inline fun <reified S : Any, reified E : Any> HttpClient.safeRequest(
    block: HttpRequestBuilder.() -> Unit
): NetworkResponse<S, E> = try {
    val response = this.request { block() }
    if (response.status.isSuccess()) {
        NetworkResponse.Success(response.body<S>())
    } else {
        val rawBody = runCatching { response.bodyAsText() }.getOrElse { "<unable to read error body: ${it.message}>" }
        println(
            "HTTP ApiError -> status=${response.status.value}, body=$rawBody"
        )
        val parsed = runCatching {
            Json { ignoreUnknownKeys = true }.decodeFromString<E>(rawBody)
        }.getOrNull()
        if (parsed != null) {
            NetworkResponse.ApiError(parsed, response.status.value)
        } else {
            NetworkResponse.UnknownError(
                IllegalStateException(
                    "ApiError parse failed. status=${response.status.value}, body=$rawBody",
                ),
            )
        }
    }
} catch (e: Throwable) {
    // In KMP, we use Throwable as the base for network/parsing errors.
    NetworkResponse.NetworkError(e)
}
