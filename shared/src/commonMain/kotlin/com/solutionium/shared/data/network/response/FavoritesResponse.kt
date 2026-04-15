package com.solutionium.shared.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoritesResponse(
    @SerialName("success")
    val success: Boolean,
    @SerialName("data")
    val data: FavoritesResponseData? = null,
)

@Serializable
data class FavoritesResponseData(
    @SerialName("ids")
    val ids: List<Int> = emptyList(),
    @SerialName("updated_at")
    val updatedAt: Long = 0L,
)
