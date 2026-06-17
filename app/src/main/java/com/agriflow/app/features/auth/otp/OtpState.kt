/**
 * UI State definition representing the screen state for Otp.
 */
package com.agriflow.app.features.auth.otp

data class OtpState(
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
