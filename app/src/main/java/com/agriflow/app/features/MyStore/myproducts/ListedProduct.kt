/**
 * Represents the class [ProductStatus] providing core functionality within the application.
 */
package com.agriflow.app.features.MyStore.myproducts

enum class ProductStatus {
    ACTIVE,
    INACTIVE,
    OUT_OF_STOCK,
    UNDER_REVIEW
}

data class ListedProduct(
    val id: String,
    val name: String,
    val price: Double,
    val stockQuantity: Int,
    val unit: String,
    val imageUrl: String,
    val status: ProductStatus
)
