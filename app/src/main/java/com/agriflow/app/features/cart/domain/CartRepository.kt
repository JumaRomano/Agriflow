package com.agriflow.app.features.cart.domain

import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result

interface CartRepository {
    suspend fun getCart(): Result<Cart, DataError.Network>
    suspend fun addToCart(productId: String, quantity: Double): Result<Cart, DataError.Network>
    suspend fun removeCartItem(itemId: String): Result<Unit, DataError.Network>
    suspend fun deductCartItem(itemId: String, quantity: Double): Result<Cart, DataError.Network>
    suspend fun clearCart(): Result<Unit, DataError.Network>
}
