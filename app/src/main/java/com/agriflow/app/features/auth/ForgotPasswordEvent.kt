/**
 * Sealed interface representing one-shot UI events emitted by the ForgotPassword ViewModel.
 */
package com.agriflow.app.features.auth

sealed interface ForgotPasswordEvent {
    data class NavigateToOtp(val email: String, val type: String) : ForgotPasswordEvent
    data class ShowMessage(val message: String) : ForgotPasswordEvent
}
