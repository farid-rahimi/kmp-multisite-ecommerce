package com.solutionium.shared.data.model

data class FavoriteSyncSnapshot(
    val ids: Set<Int>,
    val updatedAt: Long,
)
