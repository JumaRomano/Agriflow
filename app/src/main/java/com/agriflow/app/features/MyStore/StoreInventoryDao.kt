package com.agriflow.app.features.MyStore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreInventoryDao {
    @Query("SELECT * FROM store_inventories ORDER BY createdAt DESC")
    fun observeAllInventory(): Flow<List<StoreInventoryEntity>>

    @Query("SELECT * FROM store_inventories WHERE syncStatus = 'PENDING'")
    suspend fun getUnsyncedInventory(): List<StoreInventoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItems(items: List<StoreInventoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: StoreInventoryEntity)

    @Query("UPDATE store_inventories SET syncStatus = :newStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String, newStatus: String)

    @Query("DELETE FROM store_inventories WHERE id = :id")
    suspend fun deleteInventoryItem(id: String)
}
