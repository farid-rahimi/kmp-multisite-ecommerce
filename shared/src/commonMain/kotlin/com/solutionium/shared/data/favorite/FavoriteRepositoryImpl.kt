package com.solutionium.shared.data.favorite

import com.russhwolf.settings.Settings
import com.solutionium.shared.data.api.woo.WooFavoriteRemoteSource
import com.solutionium.shared.data.database.dao.FavoriteDao
import com.solutionium.shared.data.local.TokenStore
import com.solutionium.shared.data.model.Favorite
import com.solutionium.shared.data.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

class FavoriteRepositoryImpl(
    private val favoriteDao: FavoriteDao,
    private val remoteDataSource: WooFavoriteRemoteSource,
    private val tokenStore: TokenStore,
    private val settings: Settings,
) : FavoriteRepository {

    companion object {
        private const val KEY_OWNER = "favorites_owner_id"
        private const val KEY_LOCAL_VERSION_SEQ = "favorites_local_version_seq"
    }

    private fun ownerKeySuffix(owner: String) = owner.replace(':', '_')
    private fun localUpdatedKey(owner: String) = "favorites_local_updated_at_${ownerKeySuffix(owner)}"
    private fun serverUpdatedKey(owner: String) = "favorites_server_updated_at_${ownerKeySuffix(owner)}"
    private fun dirtyKey(owner: String) = "favorites_dirty_${ownerKeySuffix(owner)}"

    private fun currentOwnerId(): String = tokenStore.getUserId()?.takeIf { it.isNotBlank() } ?: "guest"

    private fun isLoggedIn(): Boolean = !tokenStore.getToken().isNullOrBlank() && !tokenStore.getUserId().isNullOrBlank()

    private suspend fun ensureOwnerScope() {
        val owner = currentOwnerId()
        val storedOwner = settings.getStringOrNull(KEY_OWNER)
        if (storedOwner != owner) {
            favoriteDao.clearAllFavorites()
            settings.putString(KEY_OWNER, owner)
            settings.putLong(localUpdatedKey(owner), 0L)
            settings.putLong(serverUpdatedKey(owner), 0L)
            settings.putBoolean(dirtyKey(owner), false)
        }
    }

    private fun markLocalDirty(owner: String) {
        settings.putLong(localUpdatedKey(owner), nextLocalVersion())
        settings.putBoolean(dirtyKey(owner), true)
    }

    private fun markSynced(owner: String, updatedAt: Long) {
        settings.putLong(localUpdatedKey(owner), updatedAt)
        settings.putLong(serverUpdatedKey(owner), updatedAt)
        settings.putBoolean(dirtyKey(owner), false)
    }

    private fun nextLocalVersion(): Long {
        val next = settings.getLong(KEY_LOCAL_VERSION_SEQ, 0L) + 1L
        settings.putLong(KEY_LOCAL_VERSION_SEQ, next)
        return next
    }

    private suspend fun replaceLocalFromServer(ids: Set<Int>, updatedAt: Long) {
        favoriteDao.replaceAllFavorites(ids.map { Favorite(productId = it).toEntity() })
        markSynced(currentOwnerId(), updatedAt)
    }

    override fun observeFavoriteIds(): Flow<List<Int>> {
        return tokenStore.observeToken().flatMapLatest {
            flow {
                ensureOwnerScope()
                if (isLoggedIn()) {
                    syncFavorites()
                }
                emitAll(favoriteDao.observeFavoriteIds())
            }
        }
    }

    override suspend fun getFavoriteIds(): Set<Int> {
        ensureOwnerScope()
        return favoriteDao.getAllFavorites().map { it.productId }.toSet()
    }

    override suspend fun toggleFavoriteStatus(productId: Int, isCurrentlyFavorite: Boolean) {
        ensureOwnerScope()
        val owner = currentOwnerId()
        if (isCurrentlyFavorite) {
            favoriteDao.removeFavorite(productId)
        } else {
            favoriteDao.addFavorite(Favorite(productId).toEntity())
        }
        markLocalDirty(owner)

        if (!isLoggedIn()) return

        when (val remoteResult = remoteDataSource.toggleFavorite(productId, !isCurrentlyFavorite)) {
            is Result.Success -> {
                replaceLocalFromServer(
                    ids = remoteResult.data.ids,
                    updatedAt = remoteResult.data.updatedAt,
                )
            }
            is Result.Failure -> {
                // Keep dirty local state; periodic sync will retry.
            }
        }
    }

    override suspend fun syncFavorites() {
        ensureOwnerScope()
        if (!isLoggedIn()) return

        val owner = currentOwnerId()
        val localUpdatedAt = settings.getLong(localUpdatedKey(owner), 0L)
        val isDirty = settings.getBoolean(dirtyKey(owner), false)

        when (val result = remoteDataSource.getFavorites()) {
            is Result.Success -> {
                val remote = result.data
                val remoteUpdatedAt = remote.updatedAt
                val localIds = favoriteDao.getAllFavorites().map { it.productId }.toSet()

                when {
                    isDirty && localUpdatedAt >= remoteUpdatedAt -> {
                        when (val pushResult = remoteDataSource.setFavorites(localIds)) {
                            is Result.Success -> {
                                replaceLocalFromServer(pushResult.data.ids, pushResult.data.updatedAt)
                            }
                            is Result.Failure -> Unit
                        }
                    }
                    remoteUpdatedAt > localUpdatedAt || !isDirty -> {
                        replaceLocalFromServer(remote.ids, remoteUpdatedAt)
                    }
                }
            }
            is Result.Failure -> {
                // No-op, keep local cache and retry later.
            }
        }
    }
}
