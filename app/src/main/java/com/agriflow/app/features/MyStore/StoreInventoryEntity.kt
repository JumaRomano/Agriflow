package com.agriflow.app.features.MyStore

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.agriflow.app.core.database.SyncState

/**
 * Database entity representing a store inventory item or offline product draft.
 */
@Entity(tableName = "store_inventories")
data class StoreInventoryEntity(
    @PrimaryKey val id: String,
    val productName: String,
    val description: String,
    val price: Double,
    val quantity: Int,
    val unit: String,
    val categoryId: String,
    val syncStatus: SyncState,
    val createdAt: Long,
    val imageUrls: String // Comma-separated list of image URI/URLs
)
