package com.solutionium.shared.data.api.woo

import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.network.adapter.NetworkResponse
import com.solutionium.shared.data.network.response.WooErrorResponse
import com.solutionium.shared.data.model.Result
import io.github.aakira.napier.Napier

val TAG: String = "NetworkResponseHandler"

suspend fun <T : Any, R> handleNetworkResponse(
    networkCall: suspend () -> NetworkResponse<T, WooErrorResponse>,
    mapper: (T) -> R,
): Result<R, GeneralError> {

    return when (val result = networkCall()) {
        is NetworkResponse.Success -> {
            Napier.d(tag = "TAG", message = "handleNetworkResponse: success")
            val response = result.body
            if (response != null) {
                Result.Success(mapper(response))
            } else {
                Result.Failure(GeneralError.UnknownError(Throwable("Response body is null")))
            }
        }
        is NetworkResponse.ApiError -> {
            val errorResponse = result.body
            println(
                "Network ApiError -> status=${errorResponse.data?.status}, code=${errorResponse.code}, message=${errorResponse.message}"
            )
            Napier.d(tag = "TAG", message = "${errorResponse.toString()} handleNetworkResponse: api error")
            Result.Failure(GeneralError.ApiError(errorResponse.message, errorResponse.code, errorResponse.data?.status))
        }
        is NetworkResponse.NetworkError -> {
            Napier.e(tag = TAG, throwable = result.error, message = "handleNetworkResponse: NetworkError ${result.error}")
            Result.Failure(GeneralError.NetworkError)
        }
        is NetworkResponse.UnknownError -> {
            Napier.e(tag = TAG, throwable = result.error, message = "handleNetworkResponse: UnknownError")
            Result.Failure(GeneralError.UnknownError(result.error))

        }
    }
}
