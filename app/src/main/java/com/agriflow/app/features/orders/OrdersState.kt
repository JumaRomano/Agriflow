/**
 * UI State definition representing the screen state for Orders.
 */
package com.agriflow.app.features.orders

import com.agriflow.app.features.auth.UserRole

data class OrdersState(
    val isLoading: Boolean = false,
    val orders: List<OrderDto> = emptyList(),
    val activeRole: UserRole = UserRole.UNKNOWN,
    val expandedOrderId: String? = null,
    val errorMessage: String? = null,
    val userRole: UserRole = UserRole.UNKNOWN
)
