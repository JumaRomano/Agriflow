package com.agriflow.app.features.products.productdetails

data class ProductUpdateRequest(
    val productName: String,
    val description: String,
    val price: Double,
    val quantity: Int,
    val unit: String,
    val categoryId: String,
    val availableStock: Int = quantity
)
