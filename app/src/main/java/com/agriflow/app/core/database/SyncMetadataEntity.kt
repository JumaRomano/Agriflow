/**
 * Database entity class representing a record in the local database.
 */
package com.agriflow.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAtMillis: Long
)
