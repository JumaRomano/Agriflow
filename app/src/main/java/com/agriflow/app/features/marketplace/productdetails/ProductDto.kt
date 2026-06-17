/**
 * Data Transfer Object (DTO) used for network request/response serialization.
 */
package com.agriflow.app.features.marketplace.productdetails

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("id") val id: String?,
    @SerializedName("productName") val name: String?,
    @SerializedName("categoryName") val category: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("businessName") val companyName: String?,
    @SerializedName("images") val images: List<String>?,
    @SerializedName("quantity") val availableQuantity: Double?,
    @SerializedName("unit") val quantityUnit: String?,
    @SerializedName("description") val description: String?
)

data class PaginatedProductResponse(
    @SerializedName("content") val content: List<ProductDto>
)
