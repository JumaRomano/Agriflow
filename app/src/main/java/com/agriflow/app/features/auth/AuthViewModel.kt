/**
 * ViewModel managing the business logic and UI state for the Auth feature.
 */
package com.agriflow.app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.otp.OtpType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val staffAuthRepository: com.agriflow.app.features.staff.auth.StaffAuthRepository
) : ViewModel() {

    // Mutable inside the ViewModel, read-only for the UI.
    // This protects the state from being changed directly by Composables.
    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    // Channel is used for one-time events like navigation and snackbars.
    // Regular StateFlow is not ideal for these because it replays the latest value.
    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: AuthAction) {
        // UDF entry point: every UI interaction comes through here.
        // When adding a new field, add its action here and update AuthState with copy().
        when (action) {
            is AuthAction.LoginEmailChanged -> {
                _state.update { it.copy(loginEmail = action.email, errorMessage = null) }
            }

            is AuthAction.LoginPasswordChanged -> {
                _state.update { it.copy(loginPassword = action.password, errorMessage = null) }
            }

            AuthAction.LoginSubmitted -> login()

            is AuthAction.RegisterusernameChanged -> {
                _state.update { it.copy(registerusername = action.username, errorMessage = null) }
            }

            is AuthAction.RegisterEmailChanged -> {
                _state.update { it.copy(registerEmail = action.email, errorMessage = null) }
            }

            is AuthAction.RegisterPhoneNumberChanged -> {
                _state.update { it.copy(registerPhoneNumber = action.phoneNumber, errorMessage = null) }
            }

            is AuthAction.RegisterPasswordChanged -> {
                _state.update { it.copy(registerPassword = action.password, errorMessage = null) }
            }

            is AuthAction.RegisterTermsAcceptedChanged -> {
                _state.update { it.copy(registerTermsAccepted = action.accepted, errorMessage = null) }
            }

            is AuthAction.RegisterfirstNameChanged ->{
                _state.update { it.copy(registerfirstName = action.firstName, errorMessage = null) }
            }

            is AuthAction.RegistersurNameChanged ->{
                _state.update { it.copy(registersurName = action.surName, errorMessage = null) }
            }

            AuthAction.RegisterSubmitted -> register()
        }
    }

    private fun login() {
        val currentState = state.value
        val validationMessage = validateLogin(currentState)

        // Keep validation before the network call so the backend only receives clean requests.
        if (validationMessage != null) {
            showValidationMessage(validationMessage)
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val isEmail = currentState.loginEmail.contains("@")

            val result = if (isEmail) {
                authRepository.login(
                    email = currentState.loginEmail,
                    password = currentState.loginPassword
                )
            } else {
                staffAuthRepository.login(
                    username = currentState.loginEmail,
                    password = currentState.loginPassword
                )
            }

            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    if (result.data.mustChangePassword) {
                        _events.send(AuthEvent.NavigateToChangePassword(currentState.loginPassword))
                    } else if (isEmail) {
                        _events.send(AuthEvent.NavigateToMain)
                    } else {
                        _events.send(AuthEvent.NavigateToStaffDashboard)
                    }
                }

                is Result.Error -> {
                    val message = result.error.toMessage()
                    _state.update { it.copy(isLoading = false, errorMessage = message) }
                    _events.send(AuthEvent.ShowMessage(message))
                }
            }
        }
    }

    private fun register() {
        val currentState = state.value
        val validationMessage = validateRegister(currentState)

        // Add or remove registration field rules here when the backend contract changes.
        if (validationMessage != null) {
            showValidationMessage(validationMessage)
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (
                val result = authRepository.register(
                    username = currentState.registerusername,
                    email = currentState.registerEmail,
                    phoneNumber = currentState.registerPhoneNumber,
                    password = currentState.registerPassword,
                    firstName = currentState.registerfirstName,
                    surName = currentState.registersurName
                )
            ) {
                is Result.Success -> {
                    when (val otpResult = authRepository.sendOtp(currentState.registerEmail, OtpType.REGISTRATION)) {
                        is Result.Success -> {
                            _state.update { it.copy(isLoading = false) }
                            _events.send(AuthEvent.NavigateToOtp(currentState.registerEmail, OtpType.REGISTRATION.name))
                        }
                        is Result.Error -> {
                            val message = otpResult.error.toMessage()
                            _state.update { it.copy(isLoading = false, errorMessage = message) }
                            _events.send(AuthEvent.ShowMessage(message))
                        }
                    }
                }

                is Result.Error -> {
                    val message = result.error.toMessage()
                    _state.update { it.copy(isLoading = false, errorMessage = message) }
                    _events.send(AuthEvent.ShowMessage(message))
                }
            }
        }
    }

    private fun validateLogin(state: AuthState): String? {
        return when {
            state.loginEmail.isBlank() -> "Email is required"
            state.loginPassword.isBlank() -> "Password is required"
            else -> null
        }
    }

    private fun validateRegister(state: AuthState): String? {
        return when {
            state.registerusername.isBlank() -> "Username is required"
            state.registerfirstName.isBlank() -> "First name is required"
            state.registersurName.isBlank() -> "Surname is required"
            state.registerEmail.isBlank() -> "Email is required"
            !state.registerEmail.contains("@") -> "Enter a valid email address"
            state.registerPassword.length < MIN_PASSWORD_LENGTH -> {
                "Password must be at least $MIN_PASSWORD_LENGTH characters"
            }
            !state.registerTermsAccepted -> "Accept the terms to create an account"
            else -> null
        }
    }

    private fun showValidationMessage(message: String) {
        _state.update { it.copy(errorMessage = message) }
        viewModelScope.launch {
            _events.send(AuthEvent.ShowMessage(message))
        }
    }

    private fun DataError.Network.toMessage(): String {
        // Converts technical data-layer errors into user-facing text.
        // Keep backend/server wording out of the UI where possible.
        return when (this) {
            DataError.Network.REQUEST_TIMEOUT -> "The request timed out. Check your connection and try again."
            DataError.Network.UNAUTHORIZED -> "Incorrect email or password."
            DataError.Network.CONFLICT -> "An account with these details already exists."
            DataError.Network.TOO_MANY_REQUESTS -> "Too many attempts. Try again shortly."
            DataError.Network.NO_INTERNET -> "No internet connection."
            DataError.Network.PAYLOAD_TOO_LARGE -> "The request is too large."
            DataError.Network.SERVER_ERROR -> "The server is unavailable. Try again later."
            DataError.Network.SERIALIZATION -> "The server response was not in the expected format."
            DataError.Network.UNKNOWN -> "Something went wrong. Try again."
            DataError.Network.NOT_FOUND -> "Not Found"
        }
    }

    private fun UserRole.requiresBusinessName(): Boolean {
        return this == UserRole.FARMER || this == UserRole.SUPPLIER
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
