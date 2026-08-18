package com.agriflow.app.features.cart.presentation

import com.agriflow.app.features.cart.domain.CartItem

data class CartState(
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val shippingFee: Double = 0.0,
    val total: Double = 0.0,
    val isFetchingCart: Boolean = false,
    val isClearingCart: Boolean = false,
    val errorMessage: String? = null,
    val updatingItemIds: Set<String> = emptySet() // Track specific items updating or deleting to show granular row spinners
)
