/**
 * UI State definition representing the screen state for Auth.
 */
package com.agriflow.app.features.auth

import com.agriflow.app.features.auth.UserRole
// Single source of truth for the auth UI.
// Screens read this immutable state and send AuthAction events when the user changes something.
data class AuthState(
    val loginEmail: String = "",
    val loginPassword: String = "",
    val registerusername: String = "",
    val registerEmail: String = "",
    val registerPhoneNumber: String = "",
    val registerfirstName: String = "",
    val registersurName: String = "",
    val registerPassword: String = "",
    val registerRole: UserRole = UserRole.BUYER,
    val registerTermsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
