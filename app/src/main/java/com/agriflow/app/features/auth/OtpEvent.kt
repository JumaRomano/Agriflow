/**
 * Sealed interface representing one-shot UI events emitted by the Otp ViewModel.
 */
package com.agriflow.app.features.auth

sealed interface OtpEvent {
    data object NavigateToLogin : OtpEvent
    data class NavigateToResetPassword(val token: String) : OtpEvent
    data class ShowMessage(val message: String) : OtpEvent
}
