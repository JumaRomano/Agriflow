package com.agriflow.app.features.staff.auth

import com.google.gson.annotations.SerializedName

data class StaffLoginRequestDto(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

// We can reuse the normal AuthResponseDto if the server returns the same token structure.
// Let's assume the backend returns the standard AuthResponseDto with token and user info.
