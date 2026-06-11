package com.agriflow.app.features.auth

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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpVerificationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val routeArgs = savedStateHandle.toRoute<Route.OtpVerificationRoute>()
    val email = routeArgs.email
    val type: OtpType = try {
        OtpType.valueOf(routeArgs.type)
    } catch (_: Exception) {
        OtpType.REGISTRATION
    }

    private val _state = MutableStateFlow(OtpState())
    val state = _state.asStateFlow()

    private val _events = Channel<OtpEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: OtpAction) {
        when (action) {
            is OtpAction.OnOtpChanged -> {
                _state.update { it.copy(otpCode = action.otp, error = null) }
            }
            OtpAction.VerifyClicked -> verifyOtp()
            OtpAction.ResendClicked -> resendOtp()
        }
    }

    private fun verifyOtp() {
        val code = _state.value.otpCode
        if (code.length < 4) {
            _state.update { it.copy(error = "Enter a valid code") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.verifyOtp(email, code, type)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    val response = result.data
                    if (response.verified) {
                        if (type == OtpType.FORGOT_PASSWORD) {
                            val token = response.resetToken
                            if (!token.isNullOrBlank()) {
                                _events.send(OtpEvent.NavigateToResetPassword(token))
                            } else {
                                _state.update { it.copy(error = "Invalid reset token received from server") }
                            }
                        } else {
                            _events.send(OtpEvent.NavigateToLogin)
                        }
                    } else {
                        _state.update { it.copy(error = "OTP code could not be verified") }
                    }
                }
                is Result.Error -> {
                    val message = result.error.toMessage()
                    _state.update { it.copy(isLoading = false, error = message) }
                    _events.send(OtpEvent.ShowMessage(message))
                }
            }
        }
    }

    private fun resendOtp() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.sendOtp(email, type)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(OtpEvent.ShowMessage("OTP code resent successfully!"))
                }
                is Result.Error -> {
                    val message = result.error.toMessage()
                    _state.update { it.copy(isLoading = false, error = message) }
                    _events.send(OtpEvent.ShowMessage(message))
                }
            }
        }
    }

    private fun DataError.Network.toMessage(): String {
        return when (this) {
            DataError.Network.REQUEST_TIMEOUT -> "The request timed out. Check your connection and try again."
            DataError.Network.UNAUTHORIZED -> "Invalid OTP verification code."
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
