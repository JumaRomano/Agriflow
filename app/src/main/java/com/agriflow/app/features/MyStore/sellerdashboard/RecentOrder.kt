/**
 * Represents the class [OrderStatus] providing core functionality within the application.
 */
package com.agriflow.app.features.MyStore.sellerdashboard

enum class OrderStatus {
    PENDING,
    COMPLETED,
    SHIPPED,
    CANCELLED
}

data class RecentOrder(
    val id: String,
    val orderNumber: String,
    val createdAt: String,
    val status: String,
    val totalAmount: Double,
    val itemsCount: Int
)
