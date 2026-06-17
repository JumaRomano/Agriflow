/**
 * Sealed interface representing one-shot UI events emitted by the CreateNewPassword ViewModel.
 */
package com.agriflow.app.features.auth.password

sealed interface CreateNewPasswordEvent {
    data object NavigateToLogin : CreateNewPasswordEvent
    data class ShowMessage(val message: String) : CreateNewPasswordEvent
}
