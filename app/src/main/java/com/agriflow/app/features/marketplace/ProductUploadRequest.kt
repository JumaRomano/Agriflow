/**
 * Data Transfer Object (DTO) used for network request/response serialization.
 */
package com.agriflow.app.features.marketplace

data class ProductUploadRequest(
    val productName: String,
    val description: String,
    val price: Double,
    val quantity: Int,
    val unit: String,
    val categoryId: String,
    val images: List<String>
)
