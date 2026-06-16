/**
 * Retrofit API interface defining network endpoints for the Orders service.
 */
package com.agriflow.app.features.orders

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface OrdersApi {

    @POST("orders/checkout")
    suspend fun checkout(
        @Body request: CheckoutRequestDto
    ): Response<OrderDto>

    @PATCH("orders/seller/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: String,
        @Body request: UpdateOrderStatusRequestDto
    ): Response<OrderDto>


    @GET("orders/{id}")
    suspend fun getOrderById(
        @Path("id") id: String
    ): Response<OrderDto>

    @GET("orders/my-orders")
    suspend fun getMyOrders(): Response<List<OrderDto>>

    @GET("orders/seller/my-orders")
    suspend fun getBusinessOrders(): Response<List<OrderDto>>

}
