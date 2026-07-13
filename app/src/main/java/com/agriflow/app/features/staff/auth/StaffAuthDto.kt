package com.agriflow.app.features.staff.auth

import com.google.gson.annotations.SerializedName

data class StaffLoginRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class VerifyAssignmentRequest(
    @SerializedName("evidencePhotos") val evidencePhotos: List<String>,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)


