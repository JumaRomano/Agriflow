/**
 * Data Transfer Object (DTO) used for network request serialization when updating product details via PATCH.
 */
package com.agriflow.app.features.marketplace

data class ProductUpdateRequest(
    val productName: String,
    val description: String,
    val price: Double,
    val quantity: Int,
    val unit: String,
    val categoryId: String
)
