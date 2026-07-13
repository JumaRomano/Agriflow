/**
 * ViewModel managing the business logic and UI state for the CreateNewPassword feature.
 */
package com.agriflow.app.features.auth.password

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.agriflow.app.core.navigation.Route
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNewPasswordViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val routeArgs = savedStateHandle.toRoute<Route.CreateNewPasswordRoute>()
    val email = routeArgs.email
    val resetToken = routeArgs.resetToken

    private val _state = MutableStateFlow(CreateNewPasswordState())
    val state = _state.asStateFlow()

    private val _events = Channel<CreateNewPasswordEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: CreateNewPasswordAction) {
        when (action) {
            is CreateNewPasswordAction.OnNewPasswordChanged -> {
                _state.update { it.copy(newPassword = action.password, error = null) }
            }
            is CreateNewPasswordAction.OnConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = action.password, error = null) }
            }
            CreateNewPasswordAction.SubmitClicked -> submitReset()
        }
    }

    private fun submitReset() {
        val newPassword = _state.value.newPassword
        val confirmPassword = _state.value.confirmPassword

        val validationError = when {
            newPassword.length < MIN_PASSWORD_LENGTH -> {
                "Password must be at least $MIN_PASSWORD_LENGTH characters"
            }
            newPassword != confirmPassword -> {
                "Passwords do not match"
            }
            else -> null
        }

        if (validationError != null) {
            _state.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.passwordReset(email, newPassword, confirmPassword, resetToken)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(CreateNewPasswordEvent.ShowMessage("Password reset successfully!"))
                    _events.send(CreateNewPasswordEvent.NavigateToLogin)
                }
                is Result.Error -> {
                    val message = result.error.toMessage()
                    _state.update { it.copy(isLoading = false, error = message) }
                    _events.send(CreateNewPasswordEvent.ShowMessage(message))
                }
            }
        }
    }

    private fun DataError.Network.toMessage(): String {
        return when (this) {
            DataError.Network.REQUEST_TIMEOUT -> "The request timed out. Check your connection and try again."
            DataError.Network.UNAUTHORIZED -> "Unauthorized request or token expired."
            DataError.Network.CONFLICT -> "A conflict occurred."
            DataError.Network.TOO_MANY_REQUESTS -> "Too many attempts. Try again shortly."
            DataError.Network.NO_INTERNET -> "No internet connection."
            DataError.Network.PAYLOAD_TOO_LARGE -> "The request is too large."
            DataError.Network.SERVER_ERROR -> "The server is unavailable. Try again later."
            DataError.Network.SERIALIZATION -> "The server response was not in the expected format."
            DataError.Network.UNKNOWN -> "Something went wrong. Try again."
            DataError.Network.NOT_FOUND -> "Not Found"
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
