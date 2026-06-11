package com.agriflow.app.features.auth

sealed interface ForgotPasswordAction {
    data class OnEmailChanged(val email: String) : ForgotPasswordAction
    data object SendOtpClicked : ForgotPasswordAction
}
