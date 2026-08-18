package com.agriflow.app.features.auth.otp
sealed interface OtpAction {
    data class OnOtpChanged(val otp: String) : OtpAction
    data object VerifyClicked : OtpAction
    data object ResendClicked : OtpAction
}