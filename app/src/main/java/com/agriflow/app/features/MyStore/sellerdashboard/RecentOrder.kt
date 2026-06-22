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
    val productName: String,
    val price: Double,
    val statusEnum: OrderStatus
)
