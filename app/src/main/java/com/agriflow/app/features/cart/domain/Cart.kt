/**
 * Represents the class [Cart] providing core functionality within the application.
 */
package com.agriflow.app.features.cart.domain

data class Cart(
    val items: List<CartItem>,
    val subtotal: Double,
    val shippingFee: Double,
    val total: Double
)
