package com.solutionium.shared.data.api.woo.impl

import com.solutionium.shared.data.api.woo.TAG
import com.solutionium.shared.data.api.woo.handleNetworkResponse
import com.solutionium.shared.data.local.TokenStore
import com.solutionium.shared.data.model.FavoriteSyncSnapshot
import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.api.woo.WooFavoriteRemoteSource
import com.solutionium.shared.data.network.clients.UserClient
import com.solutionium.shared.data.model.Result
import io.github.aakira.napier.Napier


class WooFavoriteRemoteSourceImpl(

    private val apiService: UserClient,
    private val tokenStore: TokenStore,

) : WooFavoriteRemoteSource {
    override suspend fun getFavorites(): Result<FavoriteSyncSnapshot, GeneralError> {
        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            return Result.Failure(GeneralError.ApiError("Authentication required.", "rest_not_logged_in", 401))
        }
        return handleNetworkResponse(
            networkCall = { apiService.getFavorites("Bearer $token") },
            mapper = { response ->
                FavoriteSyncSnapshot(
                    ids = response.data?.ids?.toSet() ?: emptySet(),
                    updatedAt = response.data?.updatedAt ?: 0L,
                )
            },
        )
    }

    override suspend fun toggleFavorite(
        productId: Int,
        isFavorite: Boolean,
    ): Result<FavoriteSyncSnapshot, GeneralError> {
        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            return Result.Failure(GeneralError.ApiError("Authentication required.", "rest_not_logged_in", 401))
        }
        Napier.d(tag = TAG, message = "toggleFavorite remote sync: productId=$productId isFavorite=$isFavorite")
        return handleNetworkResponse(
            networkCall = { apiService.toggleFavorite("Bearer $token", productId, isFavorite) },
            mapper = { response ->
                FavoriteSyncSnapshot(
                    ids = response.data?.ids?.toSet() ?: emptySet(),
                    updatedAt = response.data?.updatedAt ?: 0L,
                )
            },
        )
    }

    override suspend fun setFavorites(ids: Set<Int>): Result<FavoriteSyncSnapshot, GeneralError> {
        val token = tokenStore.getToken()
        if (token.isNullOrBlank()) {
            return Result.Failure(GeneralError.ApiError("Authentication required.", "rest_not_logged_in", 401))
        }
        return handleNetworkResponse(
            networkCall = { apiService.setFavorites("Bearer $token", ids) },
            mapper = { response ->
                FavoriteSyncSnapshot(
                    ids = response.data?.ids?.toSet() ?: emptySet(),
                    updatedAt = response.data?.updatedAt ?: 0L,
                )
            },
        )
    }
}
