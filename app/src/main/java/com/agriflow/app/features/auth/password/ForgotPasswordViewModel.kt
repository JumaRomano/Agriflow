/**
 * ViewModel managing the business logic and UI state for the ForgotPassword feature.
 */
package com.agriflow.app.features.auth.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.AuthRepository
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
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state.asStateFlow()

    private val _events = Channel<ForgotPasswordEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: ForgotPasswordAction) {
        when (action) {
            is ForgotPasswordAction.OnEmailChanged -> {
                _state.update { it.copy(email = action.email, emailError = null) }
            }
            ForgotPasswordAction.SendOtpClicked -> sendOtp()
        }
    }

    private fun sendOtp() {
        val email = _state.value.email
        val emailError = when {
            email.isBlank() -> "Email is required"
            !email.contains("@") -> "Enter a valid email address"
            else -> null
        }

        if (emailError != null) {
            _state.update { it.copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, emailError = null) }
            when (val result = authRepository.sendOtp(email, OtpType.FORGOT_PASSWORD)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(ForgotPasswordEvent.NavigateToOtp(email, OtpType.FORGOT_PASSWORD.name))
                }
                is Result.Error -> {
                    val message = result.error.toMessage()
                    _state.update { it.copy(isLoading = false) }
                    _events.send(ForgotPasswordEvent.ShowMessage(message))
                }
            }
        }
    }

    private fun DataError.Network.toMessage(): String {
        return when (this) {
            DataError.Network.REQUEST_TIMEOUT -> "The request timed out. Check your connection and try again."
            DataError.Network.UNAUTHORIZED -> "Unauthorized access."
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
}
