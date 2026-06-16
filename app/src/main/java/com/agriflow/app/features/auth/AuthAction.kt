/**
 * Sealed interface representing user actions and UI events for the Auth flow.
 */
package com.agriflow.app.features.auth

import com.agriflow.app.features.auth.UserRole
// User intents from the UI. The Composables do not mutate state directly;
// they send one of these actions and AuthViewModel decides how state changes.
sealed interface AuthAction {
    data class LoginEmailChanged(val email: String) : AuthAction
    data class LoginPasswordChanged(val password: String) : AuthAction
    data object LoginSubmitted : AuthAction

    data class RegisterusernameChanged(val username: String) : AuthAction
    data class RegisterEmailChanged(val email: String) : AuthAction
    data class RegisterPhoneNumberChanged(val phoneNumber: String) : AuthAction
    data class RegisterfirstNameChanged(val firstName: String) : AuthAction
    data class RegistersurNameChanged(val surName: String) : AuthAction
    data class RegisterPasswordChanged(val password: String) : AuthAction
    data class RegisterTermsAcceptedChanged(val accepted: Boolean) : AuthAction
    data object RegisterSubmitted : AuthAction
}
