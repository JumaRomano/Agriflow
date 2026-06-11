package com.agriflow.app.features.auth
sealed interface AuthEvent {
    data object NavigateToMain : AuthEvent
    data class NavigateToOtp(val email: String, val type: String) : AuthEvent
    data class ShowMessage(val message: String) : AuthEvent
}
