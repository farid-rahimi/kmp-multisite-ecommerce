package com.solutionium.shared.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.solutionium.shared.data.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT productId FROM favorites")
    fun observeFavoriteIds(): Flow<List<Int>>

    @Query("SELECT * FROM favorites")
    suspend fun getAllFavorites(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorites(favorites: List<FavoriteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE productId = :productId")
    suspend fun removeFavorite(productId: Int)

    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()

    @Transaction
    suspend fun replaceAllFavorites(favorites: List<FavoriteEntity>) {
        clearAllFavorites()
        if (favorites.isNotEmpty()) {
            addFavorites(favorites)
        }
    }
}
