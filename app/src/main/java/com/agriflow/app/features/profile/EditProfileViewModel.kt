/**
 * ViewModel for the Edit Profile feature.
 * Coordinates profile loading, form validation, and calling the update API.
 */
package com.agriflow.app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
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
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state = _state.asStateFlow()

    private val _events = Channel<EditProfileEvent>()
    val events = _events.receiveAsFlow()

    private var isInitialized = false

    init {
        viewModelScope.launch {
            tokenRepository.getUserFlow().collect { user ->
                if (user != null && !isInitialized) {
                    isInitialized = true
                    _state.update {
                        it.copy(
                            username = user.username,
                            email = user.email,
                            phoneNumber = user.phoneNumber ?: ""
                        )
                    }
                }
            }
        }
    }

    fun onAction(action: EditProfileAction) {
        when (action) {
            is EditProfileAction.OnUsernameChanged -> {
                _state.update { it.copy(username = action.username, usernameError = null) }
            }
            is EditProfileAction.OnPhoneNumberChanged -> {
                _state.update { it.copy(phoneNumber = action.phoneNumber, phoneNumberError = null) }
            }
            EditProfileAction.OnSaveClicked -> saveProfile()
        }
    }

    private fun saveProfile() {
        val currentUsername = _state.value.username
        val currentPhoneNumber = _state.value.phoneNumber

        var hasError = false

        if (currentUsername.isBlank()) {
            _state.update { it.copy(usernameError = "Name cannot be empty") }
            hasError = true
        } else if (currentUsername.trim().length < 3) {
            _state.update { it.copy(usernameError = "Name must be at least 3 characters") }
            hasError = true
        }

        if (currentPhoneNumber.isNotBlank()) {
            val cleanPhone = currentPhoneNumber.trim()
            val isValid = cleanPhone.matches(Regex("^\\+?[0-9]{7,15}$"))
            if (!isValid) {
                _state.update { it.copy(phoneNumberError = "Enter a valid phone number") }
                hasError = true
            }
        }

        if (hasError) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = authRepository.updateProfile(
                username = currentUsername,
                phoneNumber = currentPhoneNumber.trim().takeIf { it.isNotBlank() }
            )
            _state.update { it.copy(isLoading = false) }

            when (result) {
                is Result.Success -> {
                    _state.update { it.copy(success = true) }
                    _events.send(EditProfileEvent.SaveSuccess)
                }
                is Result.Error -> {
                    val message = result.error.toMessage()
                    _events.send(EditProfileEvent.ShowMessage(message))
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
        }
    }
}
