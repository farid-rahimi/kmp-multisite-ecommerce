package com.solutionium.shared.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.solutionium.shared.data.database.entity.AddressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity): Long

    @Query("SELECT * FROM address WHERE userId = :userId")
    fun getAllAddress(userId: String): Flow<List<AddressEntity>>

    @Query("SELECT * FROM address WHERE id = :id AND userId = :userId LIMIT 1")
    fun getAddressById(id: Int, userId: String): Flow<AddressEntity?>


    @Query("DELETE FROM address WHERE id = :id AND userId = :userId")
    suspend fun deleteAddressById(id: Int, userId: String)

    @Query("DELETE FROM address WHERE userId = :userId")
    suspend fun deleteAllAddressesByUserId(userId: String)

    @Query("UPDATE address SET isDefault = :isDefault WHERE userId = :userId")
    suspend fun updateAllDefaultAddress(userId: String, isDefault: Boolean)

    @Query("UPDATE address SET isDefault = :isDefault WHERE id = :id AND userId = :userId")
    suspend fun updateDefaultAddress(id: Int, userId: String, isDefault: Boolean)

    @Update
    suspend fun updateAddress(address: AddressEntity)


//    @Query("UPDATE address SET isDefault = 0 WHERE isDefault = 1 AND userId = :userId")
//    suspend fun clearDefaultAddressForUser(userId: String) // Or Int, depending on your ID type

    @Query("UPDATE address SET isDefault = 1 WHERE id = (SELECT id FROM address WHERE userId = :userId ORDER BY id ASC LIMIT 1)")
    suspend fun setFirstAddressAsDefaultForUser(userId: String)

    // You would call these two within a @Transaction method in a Repository or ViewModel
//    @Transaction
//    suspend fun resetAndSetFirstAsDefaultForUser(userId: String) {
//        clearDefaultAddressForUser(userId)
//        setFirstAddressAsDefaultForUser(userId)
//        // You might want to return the new default address ID or object here
//    }


}
