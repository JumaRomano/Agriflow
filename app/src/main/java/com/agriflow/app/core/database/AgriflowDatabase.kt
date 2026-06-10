package com.agriflow.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.agriflow.app.core.database.SyncMetadataEntity
import com.agriflow.app.features.marketplace.ProductDao
import com.agriflow.app.features.marketplace.ProductEntity
@Database(
    entities = [
        SyncMetadataEntity::class,
        ProductEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AgriflowDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}
