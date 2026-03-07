package com.solutionium.shared.data.database.entity

import androidx.room.Entity
import com.solutionium.shared.data.model.ValidationInfo
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Entity class for cart item.
 */
@Entity(tableName = "cart_item", primaryKeys = ["productId", "variationId"])
@OptIn(ExperimentalTime::class)
data class CartItemEntity(
    val productId: Int,
    val variationId: Int, // Use 0 for simple products, non-zero for variations
    val isDecant: Boolean = false,
    val decVol: String? = null,
    val categoryIDs: List<Int> = emptyList(),
    val brandIDs: List<Int> = emptyList(),
    val variationAttributes: List<VariationAttributeSerializable>, // JSON string of selected attributes for variations
    var quantity: Int,
    val name: String,
    var currentPrice: Double,
    var currentStock: Int?,      // Stock at the time of adding or last validation
    var regularPrice: Double? = null,
    val manageStock: Boolean = true,
    val stockStatus: String = "instock",
    val addedAt: Long = Clock.System.now().toEpochMilliseconds(),
    // Optional: For more detailed info or if variations are complex

    val imageUrl: String,

    var requiresAttention: Boolean = false,
    // Stores "dynamic" or "resource" to know the type
    val validationInfo: ValidationInfo? = null,
    val shippingClass: String = "",
    val appOffer: Double = 0.0
)
