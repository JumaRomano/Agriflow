package com.agriflow.app.features.staff.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.agriflow.app.core.navigation.Route
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StaffChangePasswordState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface StaffChangePasswordAction {
    data class OnNewPasswordChanged(val password: String) : StaffChangePasswordAction
    data class OnConfirmPasswordChanged(val password: String) : StaffChangePasswordAction
    data object SubmitClicked : StaffChangePasswordAction
}

sealed interface StaffChangePasswordEvent {
    data object NavigateToDashboard : StaffChangePasswordEvent
    data class ShowMessage(val message: String) : StaffChangePasswordEvent
}

@HiltViewModel
class StaffChangePasswordViewModel @Inject constructor(
    private val staffAuthRepository: StaffAuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(StaffChangePasswordState())
    val state: StateFlow<StaffChangePasswordState> = _state.asStateFlow()

    private val _events = Channel<StaffChangePasswordEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        val args = savedStateHandle.toRoute<Route.StaffChangePassword>()
        _state.update { it.copy(currentPassword = args.currentPassword) }
    }

    fun onAction(action: StaffChangePasswordAction) {
        when (action) {
            is StaffChangePasswordAction.OnNewPasswordChanged -> {
                _state.update { it.copy(newPassword = action.password, error = null) }
            }
            is StaffChangePasswordAction.OnConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = action.password, error = null) }
            }
            StaffChangePasswordAction.SubmitClicked -> {
                submit()
            }
        }
    }

    private fun submit() {
        val current = _state.value.currentPassword
        val newPass = _state.value.newPassword
        val confirmPass = _state.value.confirmPassword

        val validationError = when {
            newPass.isBlank() -> "New password cannot be empty"
            newPass.length < 6 -> "Password must be at least 6 characters"
            newPass == current -> "New password must be different from your temporary password"
            newPass != confirmPass -> "Passwords do not match"
            else -> null
        }

        if (validationError != null) {
            _state.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = staffAuthRepository.changePassword(current, newPass, confirmPass)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(StaffChangePasswordEvent.ShowMessage("Password changed successfully!"))
                    _events.send(StaffChangePasswordEvent.NavigateToDashboard)
                }
                is Result.Error -> {
                    val message = result.error.toMessage()
                    _state.update { it.copy(isLoading = false, error = message) }
                    _events.send(StaffChangePasswordEvent.ShowMessage(message))
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
        }
    }
}
