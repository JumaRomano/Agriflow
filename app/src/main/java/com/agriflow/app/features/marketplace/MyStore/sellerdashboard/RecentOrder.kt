package com.agriflow.app.features.marketplace.MyStore.sellerdashboard

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
