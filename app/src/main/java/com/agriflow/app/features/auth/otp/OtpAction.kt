package com.agriflow.app.features.auth.otp

/**
 * Sealed interface representing user actions and UI events for the Otp flow.
 */
sealed interface OtpAction {
    data class OnOtpChanged(val otp: String) : OtpAction
    data object VerifyClicked : OtpAction
    data object ResendClicked : OtpAction
}