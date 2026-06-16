/**
 * Data Transfer Object (DTO) used for network request/response serialization.
 */
package com.agriflow.app.features.auth

import com.google.gson.annotations.SerializedName

data class AuthResponseDto(
    @SerializedName(value = "accessToken", alternate = ["access_token", "token"])
    val accessToken: String?,
    @SerializedName(value = "refreshToken", alternate = ["refresh_token"])
    val refreshToken: String?,
    val role: String?,
    val username: String?
)
