package com.agriflow.app.features.staff.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StaffAuthState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class StaffAuthEvent {
    object LoginSuccess : StaffAuthEvent()
    data class ShowMessage(val message: String) : StaffAuthEvent()
}

@HiltViewModel
class StaffAuthViewModel @Inject constructor(
    private val staffAuthRepository: StaffAuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StaffAuthState())
    val state: StateFlow<StaffAuthState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<StaffAuthEvent>()
    val events: SharedFlow<StaffAuthEvent> = _events.asSharedFlow()

    fun onUsernameChanged(username: String) {
        _state.update { it.copy(username = username, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password, errorMessage = null) }
    }

    fun login() {
        val currentState = _state.value
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(errorMessage = "Username and password cannot be empty.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = staffAuthRepository.login(currentState.username, currentState.password)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.emit(StaffAuthEvent.LoginSuccess)
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = getErrorMessage(result.error)) }
                    _events.emit(StaffAuthEvent.ShowMessage(getErrorMessage(result.error)))
                }
            }
        }
    }

    private fun getErrorMessage(error: DataError.Network): String {
        return when (error) {
            DataError.Network.REQUEST_TIMEOUT -> "Request timed out. Please try again."
            DataError.Network.UNAUTHORIZED -> "Invalid credentials."
            DataError.Network.CONFLICT -> "Conflict error."
            DataError.Network.TOO_MANY_REQUESTS -> "Too many requests. Try again later."
            DataError.Network.NO_INTERNET -> "No internet connection."
            DataError.Network.PAYLOAD_TOO_LARGE -> "Payload too large."
            DataError.Network.SERVER_ERROR -> "Server error occurred."
            DataError.Network.SERIALIZATION -> "Data serialization error."
            DataError.Network.UNKNOWN -> "An unknown error occurred."
        }
    }
}
