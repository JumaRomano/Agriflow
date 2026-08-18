package com.agriflow.app.features.auth.password

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null
)
