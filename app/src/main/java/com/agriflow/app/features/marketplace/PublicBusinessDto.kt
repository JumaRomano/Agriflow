package com.agriflow.app.features.marketplace

import com.google.gson.annotations.SerializedName

data class PublicBusinessDto(
    @SerializedName("id") val id: String?,
    @SerializedName("businessName") val name: String?,
    @SerializedName("businessEmail") val email: String?,
    @SerializedName("businessPhone") val phone: String?,
    @SerializedName("tagline") val tagline: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("reviewCount") val reviewCount: Int?,
    @SerializedName(value = "businessProfile", alternate = ["logoUrl", "business_profile"]) val businessProfile: String?
)
