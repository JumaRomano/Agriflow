/**
 * Data Transfer Object (DTO) used for network request/response serialization.
 */
package com.agriflow.app.features.auth

import com.google.gson.annotations.SerializedName

data class BusinessDetailsResponseDto(
    @SerializedName("id") val id: String?,
    @SerializedName("userId") val userId: String?,
    @SerializedName("businessName") val businessName: String?,
    @SerializedName("businessEmail") val businessEmail: String?,
    @SerializedName("businessPhone") val businessPhone: String?,
    @SerializedName("approvalStatus") val approvalStatus: String?, // "APPROVED", "PENDING", etc.
    @SerializedName("joinDate") val joinDate: String?,
    @SerializedName("walletBalance") val walletBalance: Double?,
    @SerializedName("pendingBalance") val pendingBalance: Double?
)
