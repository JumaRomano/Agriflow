/**
 * ViewModel for the Edit Profile feature.
 * Coordinates profile loading, form validation, and calling the update API.
 */
package com.agriflow.app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.FileHelper
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
    private val tokenRepository: TokenRepository,
    private val fileHelper: FileHelper
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state = _state.asStateFlow()

    private val _events = Channel<EditProfileEvent>()
    val events = _events.receiveAsFlow()

    private var isInitialized = false

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Immediately prefill basics from token cache
            launch {
                tokenRepository.getUserFlow().collect { user ->
                    if (user != null && !isInitialized) {
                        isInitialized = true
                        _state.update {
                            it.copy(
                                userId = user.id,
                                username = user.username,
                                email = user.email,
                                phoneNumber = user.phoneNumber ?: "",
                                firstName = user.firstName ?: "",
                                middleName = user.middleName ?: "",
                                surName = user.surName ?: "",
                                profilePicture = user.profilePicture
                            )
                        }
                    }
                }
            }

            // Fetch fresh detailed user from api
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    val user = result.data
                    _state.update {
                        it.copy(
                            userId = user.id,
                            username = user.username,
                            email = user.email,
                            phoneNumber = user.phoneNumber ?: "",
                            firstName = user.firstName ?: "",
                            middleName = user.middleName ?: "",
                            surName = user.surName ?: "",
                            profilePicture = user.profilePicture
                        )
                    }
                }
                is Result.Error -> {
                    // Fail silently, token listener already populated basics
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onAction(action: EditProfileAction) {
        when (action) {
            is EditProfileAction.OnUsernameChanged -> {
                _state.update { it.copy(username = action.username, usernameError = null) }
            }
            is EditProfileAction.OnFirstNameChanged -> {
                _state.update { it.copy(firstName = action.firstName, firstNameError = null) }
            }
            is EditProfileAction.OnMiddleNameChanged -> {
                _state.update { it.copy(middleName = action.middleName, middleNameError = null) }
            }
            is EditProfileAction.OnSurNameChanged -> {
                _state.update { it.copy(surName = action.surName, surNameError = null) }
            }
            is EditProfileAction.OnEmailChanged -> {
                _state.update { it.copy(email = action.email, emailError = null) }
            }
            is EditProfileAction.OnPhoneNumberChanged -> {
                _state.update { it.copy(phoneNumber = action.phoneNumber, phoneNumberError = null) }
            }
            is EditProfileAction.OnCurrentPasswordChanged -> {
                _state.update { it.copy(currentPassword = action.currentPassword, currentPasswordError = null) }
            }
            is EditProfileAction.OnNewPasswordChanged -> {
                _state.update { it.copy(newPassword = action.newPassword, newPasswordError = null) }
            }
            is EditProfileAction.OnConfirmNewPasswordChanged -> {
                _state.update { it.copy(confirmNewPassword = action.confirmNewPassword, confirmNewPasswordError = null) }
            }
            is EditProfileAction.OnProfilePictureSelected -> {
                uploadProfilePicture(action.uri)
            }
            EditProfileAction.OnSaveClicked -> saveProfile()
            EditProfileAction.OnChangePasswordClicked -> changePassword()
        }
    }

    private fun uploadProfilePicture(uri: android.net.Uri) {
        _state.update { it.copy(isUploadingImage = true) }
        viewModelScope.launch {
            val tempFile = fileHelper.uriToFile(uri)
            if (tempFile == null) {
                _state.update { it.copy(isUploadingImage = false) }
                _events.send(EditProfileEvent.ShowMessage("Failed to process image file."))
                return@launch
            }

            val part = fileHelper.fileToMultipartPart(tempFile)
            val uploadResult = authRepository.uploadProfileImage(part)
            
            // Clean up cache file immediately
            tempFile.delete()

            _state.update { it.copy(isUploadingImage = false) }

            when (uploadResult) {
                is Result.Success -> {
                    val url = uploadResult.data.url ?: uploadResult.data.imageUrl
                    if (!url.isNullOrBlank()) {
                        _state.update { it.copy(profilePicture = url) }
                    } else {
                        _events.send(EditProfileEvent.ShowMessage("Failed to upload image. Server returned empty URL."))
                    }
                }
                is Result.Error -> {
                    _events.send(EditProfileEvent.ShowMessage("Failed to upload image: ${uploadResult.error.name}"))
                }
            }
        }
    }

    private fun saveProfile() {
        val userId = _state.value.userId
        val currentUsername = _state.value.username
        val firstName = _state.value.firstName
        val middleName = _state.value.middleName
        val surName = _state.value.surName
        val email = _state.value.email
        val currentPhoneNumber = _state.value.phoneNumber
        val profilePicture = _state.value.profilePicture

        var hasError = false

        if (currentUsername.isBlank()) {
            _state.update { it.copy(usernameError = "Name cannot be empty") }
            hasError = true
        } else if (currentUsername.trim().length < 3) {
            _state.update { it.copy(usernameError = "Name must be at least 3 characters") }
            hasError = true
        }

        if (firstName.isBlank()) {
            _state.update { it.copy(firstNameError = "First name cannot be empty") }
            hasError = true
        }

        if (surName.isBlank()) {
            _state.update { it.copy(surNameError = "Surname cannot be empty") }
            hasError = true
        }

        if (email.isBlank()) {
            _state.update { it.copy(emailError = "Email address cannot be empty") }
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _state.update { it.copy(emailError = "Enter a valid email address") }
            hasError = true
        }

        if (currentPhoneNumber.isBlank()) {
            _state.update { it.copy(phoneNumberError = "Phone number cannot be empty") }
            hasError = true
        } else {
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
                userId = userId,
                username = currentUsername,
                firstName = firstName,
                middleName = middleName.takeIf { it.isNotBlank() },
                surName = surName,
                phoneNumber = currentPhoneNumber,
                email = email,
                profilePicture = profilePicture
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

    private fun changePassword() {
        val currentPassword = _state.value.currentPassword
        val newPassword = _state.value.newPassword
        val confirmNewPassword = _state.value.confirmNewPassword

        var hasError = false

        if (currentPassword.isEmpty()) {
            _state.update { it.copy(currentPasswordError = "Current password is required") }
            hasError = true
        }
        if (newPassword.isEmpty()) {
            _state.update { it.copy(newPasswordError = "New password is required") }
            hasError = true
        } else if (newPassword.length < 8) {
            _state.update { it.copy(newPasswordError = "Password must be at least 8 characters") }
            hasError = true
        }
        if (confirmNewPassword.isEmpty()) {
            _state.update { it.copy(confirmNewPasswordError = "Please confirm your new password") }
            hasError = true
        } else if (newPassword != confirmNewPassword) {
            _state.update { it.copy(confirmNewPasswordError = "Passwords do not match") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _state.update { it.copy(isPasswordLoading = true) }
            val result = authRepository.changePassword(
                oldPassword = currentPassword,
                newPassword = newPassword,
                confirmNewPassword = confirmNewPassword
            )
            _state.update { it.copy(isPasswordLoading = false) }

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            currentPassword = "",
                            newPassword = "",
                            confirmNewPassword = ""
                        )
                    }
                    _events.send(EditProfileEvent.ShowMessage("Password changed successfully!"))
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
