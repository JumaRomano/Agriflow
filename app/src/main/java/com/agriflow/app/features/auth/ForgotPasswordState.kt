/**
 * UI State definition representing the screen state for ForgotPassword.
 */
package com.agriflow.app.features.auth

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null
)
