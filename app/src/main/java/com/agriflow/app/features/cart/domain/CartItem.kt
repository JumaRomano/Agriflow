/**
 * Represents the class [CartItem] providing core functionality within the application.
 */
package com.agriflow.app.features.cart.domain

data class CartItem(
    val id: String,
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Double,
    val unit: String,
    val imageUrl: String?,
    val businessName: String
)
