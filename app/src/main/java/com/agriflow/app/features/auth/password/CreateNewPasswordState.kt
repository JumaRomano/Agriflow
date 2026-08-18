package com.agriflow.app.features.auth.password

data class CreateNewPasswordState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
