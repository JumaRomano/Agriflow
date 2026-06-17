/**
 * Sealed interface representing user actions and UI events for the ForgotPassword flow.
 */
package com.agriflow.app.features.auth.password

sealed interface ForgotPasswordAction {
    data class OnEmailChanged(val email: String) : ForgotPasswordAction
    data object SendOtpClicked : ForgotPasswordAction
}
