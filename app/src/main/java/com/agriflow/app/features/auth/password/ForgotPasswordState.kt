/**
 * UI State definition representing the screen state for ForgotPassword.
 */
package com.agriflow.app.features.auth.password

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null
)
