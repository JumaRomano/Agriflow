/**
 * Sealed interface representing one-shot UI events emitted by the Auth ViewModel.
 */
package com.agriflow.app.features.auth
sealed interface AuthEvent {
    data object NavigateToMain : AuthEvent
    data object NavigateToStaffDashboard : AuthEvent
    data class NavigateToOtp(val email: String, val type: String) : AuthEvent
    data class NavigateToChangePassword(val currentPassword: String) : AuthEvent
    data class ShowMessage(val message: String) : AuthEvent
}
