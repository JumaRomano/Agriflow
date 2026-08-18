package com.agriflow.app.features.auth.password

sealed interface ForgotPasswordAction {
    data class OnEmailChanged(val email: String) : ForgotPasswordAction
    data object SendOtpClicked : ForgotPasswordAction
}
