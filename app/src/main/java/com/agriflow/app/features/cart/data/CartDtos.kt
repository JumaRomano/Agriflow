package com.agriflow.app.features.cart.data

import com.google.gson.annotations.SerializedName

data class AddToCartRequestDto(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Double
)

data class DeductCartItemRequestDto(
    @SerializedName("quantity") val quantity: Double
)

data class CartItemDto(
    @SerializedName("id") val id: String?,
    @SerializedName("productId") val productId: String?,
    @SerializedName("productName") val productName: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("quantity") val quantity: Double?,
    @SerializedName("unit") val unit: String?,
    @SerializedName(value = "imageUrl", alternate = ["productImage", "image", "productImageUrl"]) val imageUrl: String?,
    @SerializedName("businessName") val businessName: String?
)

data class CartResponseDto(
    @SerializedName("items") val items: List<CartItemDto>?,
    @SerializedName("subtotal") val subtotal: Double?,
    @SerializedName("shippingFee") val shippingFee: Double?,
    @SerializedName("total") val total: Double?
)
