package com.agriflow.app.features.auth

data class OtpState(
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
