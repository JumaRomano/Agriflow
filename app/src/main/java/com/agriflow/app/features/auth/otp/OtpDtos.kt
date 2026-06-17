/**
 * Data Transfer Object (DTO) used for network request/response serialization.
 */
package com.agriflow.app.features.auth.otp

enum class OtpType {
    FORGOT_PASSWORD,
    REGISTRATION,
    RESET_PASSWORD,
    EMAIL_CHANGE,
    WITHDRAWAL
}

data class SendOtpRequestDto(
    val email: String,
    val type: OtpType
)

data class VerifyOtpRequestDto(
    val email: String,
    val otpCode: String,
    val type: OtpType
)

data class VerifyOtpResponseDto(
    val verified: Boolean,
    val resetToken: String?,
    val expiresAt: String?
)

data class PasswordResetRequestDto(
    val email: String,
    val newPassword: String,
    val confirmPassword: String,
    val resetToken: String
)
