package com.agriflow.app.features.products.productdetails

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("id") val id: String?,
    @SerializedName("productName") val name: String?,
    @SerializedName("categoryName") val category: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("businessName") val companyName: String?,
    @SerializedName("images") val images: List<String>?,
    @SerializedName("quantity") val availableQuantity: Double? = null,
    @SerializedName("unit") val quantityUnit: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("businessId") val businessId: String? = null,
    @SerializedName("availableStock") val availableStock: Double? = null,
    @SerializedName("stockStatus") val stockStatus: String? = null
)

data class PaginatedProductResponse(
    @SerializedName("content") val content: List<ProductDto>
)
