package com.agriflow.app.features.suppliernetwork

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity representing a supplier or farm in the Supplier Directory.
 */
@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val supplierId: String,
    val name: String,
    val farmLocation: String,
    val rating: Double,
    val contactInfo: String,
    val type: String, // "FARMER" or "SUPPLIER"
    val logoUrl: String?,
    val reviewCount: Int,
    val isVerified: Boolean
)
