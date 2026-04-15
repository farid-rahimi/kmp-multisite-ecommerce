package com.solutionium.shared.data.api.woo

import com.solutionium.shared.data.model.GeneralError
import com.solutionium.shared.data.model.FavoriteSyncSnapshot
import com.solutionium.shared.data.model.Result


interface WooFavoriteRemoteSource {

    suspend fun getFavorites(): Result<FavoriteSyncSnapshot, GeneralError>

    suspend fun toggleFavorite(productId: Int, isFavorite: Boolean): Result<FavoriteSyncSnapshot, GeneralError>

    suspend fun setFavorites(ids: Set<Int>): Result<FavoriteSyncSnapshot, GeneralError>

}
