/**
 * Data Transfer Object representing a Business/Supplier returned by the server.
 */
package com.agriflow.app.features.marketplace

import com.google.gson.annotations.SerializedName

data class BusinessDto(
    val id: String?,
    @SerializedName("businessName") val name: String?,
    val tagline: String?,
    val rating: Double?,
    @SerializedName("reviewCount") val reviewCount: Int?,
    @SerializedName("isVerified") val isVerified: Boolean?,
    val type: String?, // "FARMER" or "SUPPLIER"
    @SerializedName(value = "businessProfile", alternate = ["logoUrl", "business_profile"]) val logoUrl: String?
)
