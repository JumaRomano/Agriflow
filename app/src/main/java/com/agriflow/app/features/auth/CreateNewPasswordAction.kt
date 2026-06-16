/**
 * Sealed interface representing user actions and UI events for the CreateNewPassword flow.
 */
package com.agriflow.app.features.auth

sealed interface CreateNewPasswordAction {
    data class OnNewPasswordChanged(val password: String) : CreateNewPasswordAction
    data class OnConfirmPasswordChanged(val password: String) : CreateNewPasswordAction
    data object SubmitClicked : CreateNewPasswordAction
}
