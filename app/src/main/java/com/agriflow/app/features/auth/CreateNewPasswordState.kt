/**
 * UI State definition representing the screen state for CreateNewPassword.
 */
package com.agriflow.app.features.auth

data class CreateNewPasswordState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
