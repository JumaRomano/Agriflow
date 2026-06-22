/**
 * Database entity class representing a record in the local database.
 */
package com.agriflow.app.features.products.productdetails

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["category"]),
        Index(value = ["farmerName"]),
        Index(value = ["companyName"])
    ]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val priceCents: Long,
    val currencyCode: String,
    val farmerName: String,
    val imageUrl: String?,
    val availableQuantity: Double,
    val quantityUnit: String,
    val updatedAtMillis: Long,
    val companyName: String,
    val description: String,
    val businessId: String?
)
