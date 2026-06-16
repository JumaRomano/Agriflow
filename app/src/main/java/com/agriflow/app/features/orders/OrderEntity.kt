package com.agriflow.app.features.orders

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.agriflow.app.features.marketplace.MyStore.sellerdashboard.OrderStatus

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val orderNumber: String?,
    val status: OrderStatus,
    val totalAmount: Double,
    val deliveryAddress: String?,
    val deliveryNotes: String?,
    val createdAt: String?,
    val updatedAt: String?
)
