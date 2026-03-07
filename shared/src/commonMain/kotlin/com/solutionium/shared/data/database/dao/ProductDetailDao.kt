package com.solutionium.shared.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.solutionium.shared.data.database.entity.ProductDetailEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDetailDao {

    @Query("SELECT * FROM product_detail WHERE id = :productId")
    fun getProductDetails(productId: Int): Flow<ProductDetailEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductDetail(productDetail: ProductDetailEntity)
}
