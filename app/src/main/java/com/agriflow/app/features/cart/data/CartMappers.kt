package com.agriflow.app.features.cart.data

import android.util.Log
import com.agriflow.app.features.cart.domain.Cart
import com.agriflow.app.features.cart.domain.CartItem

fun CartItemDto.toDomain(): CartItem? {
    val itemId = id?.takeIf(String::isNotBlank) ?: run {
        Log.e("CartMappers", "Failed to map CartItemDto: missing or empty 'id' field.")
        return null
    }
    val prodId = productId?.takeIf(String::isNotBlank) ?: run {
        Log.e("CartMappers", "Failed to map CartItemDto: missing or empty 'productId' field.")
        return null
    }

    return CartItem(
        id = itemId,
        productId = prodId,
        name = productName?.takeIf(String::isNotBlank) ?: "Unknown Product",
        price = price ?: 0.0,
        quantity = quantity ?: 0.0,
        unit = unit?.takeIf(String::isNotBlank) ?: "kg",
        imageUrl = imageUrl?.takeIf(String::isNotBlank),
        businessName = businessName?.takeIf(String::isNotBlank) ?: "Independent Seller"
    )
}

fun CartResponseDto.toDomain(): Cart {
    val mappedItems = items?.mapNotNull { it.toDomain() } ?: emptyList()
    
    // Defensive Fallback: If subtotal is missing/zero from backend, calculate it locally
    val calculatedSubtotal = mappedItems.sumOf { it.price * it.quantity }
    val finalSubtotal = if (subtotal == null || subtotal == 0.0) calculatedSubtotal else subtotal

    // Temporarily disabled estimated shipping (set to 0.0), total equals subtotal
    val finalShippingFee = 0.0
    val finalTotal = finalSubtotal

    return Cart(
        items = mappedItems,
        subtotal = finalSubtotal,
        shippingFee = finalShippingFee,
        total = finalTotal
    )
}
