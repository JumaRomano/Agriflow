/**
 * Represents the class [Product] providing core functionality within the application.
 */
package com.agriflow.app.features.marketplace.productdetails

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val priceCents: Long,
    val currencyCode: String,
    val farmerName: String,
    val companyName: String,
    val imageUrl: String?,
    val availableQuantity: Double,
    val quantityUnit: String,
    val description: String
)
