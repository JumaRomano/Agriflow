/**
 * Repository interface for managing data transactions related to Orders.
 */
package com.agriflow.app.features.orders

import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result

interface OrdersRepository {
    suspend fun checkout(
        deliveryAddress: String,
        deliveryNotes: String?
    ): Result<OrderDto, DataError.Network>

    suspend fun updateOrderStatus(
        id: String,
        status: String,
        trackingNumber: String?,
        carrier: String?
    ): Result<OrderDto, DataError.Network>

    suspend fun getOrderById(id: String): Result<OrderDto, DataError.Network>

    suspend fun getMyOrders(): Result<List<OrderDto>, DataError.Network>

    suspend fun getBusinessOrders(): Result<List<OrderDto>, DataError.Network>
}
