package com.agriflow.app.features.auth
sealed interface AuthEvent {
    data object NavigateToMain : AuthEvent
    data class ShowMessage(val message: String) : AuthEvent
}
