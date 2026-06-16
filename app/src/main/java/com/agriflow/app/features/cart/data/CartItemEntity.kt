package com.agriflow.app.features.cart.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Double,
    val unit: String,
    val imageUrl: String?,
    val businessName: String
)
