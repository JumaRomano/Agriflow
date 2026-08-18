package com.agriflow.app.features.products.productdetails

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
    val description: String,
    val businessId: String? = null,
    val availableStock: Double? = null,
    val stockStatus: String? = null
)
