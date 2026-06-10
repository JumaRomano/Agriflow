package com.agriflow.app.features.orders

import com.google.gson.annotations.SerializedName

data class OrderDto(
    @SerializedName(value = "id", alternate = ["_id", "orderId"]) val id: String?,
    @SerializedName("userId") val userId: String?,
   // @SerializedName("businessId") val businessId: String,
    @SerializedName("orderNumber") val orderNumber: String?,
    @SerializedName(value = "status", alternate = ["fulfillmentStatus"]) val status: String?,
    @SerializedName(value = "totalAmount", alternate = ["sellerTotal"]) val totalAmount: Double?,
    @SerializedName("deliveryAddress") val deliveryAddress: String?,
    @SerializedName("deliveryNotes") val deliveryNotes: String?,
    @SerializedName("items") val items: List<OrderItemDto>?,
    @SerializedName("shipment") val shipment: ShipmentDto?,
    @SerializedName(value = "createdAt") val createdAt: String?,
    @SerializedName(value = "updatedAt") val updatedAt: String?
)

data class OrderItemDto(
    @SerializedName(value = "id", alternate = ["_id"]) val id: String?,
    @SerializedName("productId") val productId: String?,
    @SerializedName("productName") val productName: String?,
    @SerializedName(value = "productImage", alternate = ["imageUrl", "image", "productImageUrl"]) val productImage: String?,
    @SerializedName("unit") val unit: String?,
    @SerializedName(value = "price", alternate = ["unitPrice"]) val price: Double?,
    @SerializedName("quantity") val quantity: Double?,
    @SerializedName(value = "subtotal", alternate = ["totalPrice"]) val subtotal: Double?
)

data class ShipmentDto(
    @SerializedName(value = "id", alternate = ["_id"]) val id: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("trackingNumber") val trackingNumber: String?,
    @SerializedName("carrier") val carrier: String?,
    @SerializedName("estimatedDelivery") val estimatedDelivery: String?,
    @SerializedName("deliveredAt") val deliveredAt: String?
)

data class CheckoutRequestDto(
    @SerializedName("deliveryAddress") val deliveryAddress: String,
    @SerializedName("deliveryNotes") val deliveryNotes: String?
)

data class UpdateOrderStatusRequestDto(
    @SerializedName("status") val status: String,
    @SerializedName("trackingNumber") val trackingNumber: String? = null,
    @SerializedName("carrier") val carrier: String? = null
)
