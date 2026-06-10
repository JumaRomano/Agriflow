package com.agriflow.app.features.orders

import com.agriflow.app.core.network.safeApiCall
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import javax.inject.Inject

class OrdersRepositoryImpl @Inject constructor(
    private val ordersApi: OrdersApi
) : OrdersRepository {

    override suspend fun checkout(
        deliveryAddress: String,
        deliveryNotes: String?
    ): Result<OrderDto, DataError.Network> {
        return safeApiCall {
            ordersApi.checkout(
                CheckoutRequestDto(
                    deliveryAddress = deliveryAddress,
                    deliveryNotes = deliveryNotes
                )
            )
        }
    }

    override suspend fun updateOrderStatus(
        id: String,
        status: String,
        trackingNumber: String?,
        carrier: String?
    ): Result<OrderDto, DataError.Network> {
        return safeApiCall {
            ordersApi.updateOrderStatus(
                orderId = id,
                request = UpdateOrderStatusRequestDto(
                    status = status,
                    trackingNumber = trackingNumber,
                    carrier = carrier
                )
            )
        }
    }

    override suspend fun getOrderById(id: String): Result<OrderDto, DataError.Network> {
        return safeApiCall {
            ordersApi.getOrderById(id)
        }
    }

    override suspend fun getMyOrders(): Result<List<OrderDto>, DataError.Network> {
        return safeApiCall {
            ordersApi.getMyOrders()
        }
    }

    override suspend fun getBusinessOrders(): Result<List<OrderDto>, DataError.Network> {
        return safeApiCall {
            ordersApi.getBusinessOrders()
        }
    }
}
