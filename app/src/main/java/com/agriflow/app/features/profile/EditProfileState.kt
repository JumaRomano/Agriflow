/**
 * State representation for the Edit Profile UI, holding form values,
 * validation errors, and asynchronous execution states (loading, success).
 */
package com.agriflow.app.features.profile

data class EditProfileState(
    val userId: String = "",
    val username: String = "",
    val usernameError: String? = null,
    val firstName: String = "",
    val firstNameError: String? = null,
    val middleName: String = "",
    val middleNameError: String? = null,
    val surName: String = "",
    val surNameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val phoneNumber: String = "",
    val phoneNumberError: String? = null,
    val currentPassword: String = "",
    val currentPasswordError: String? = null,
    val newPassword: String = "",
    val newPasswordError: String? = null,
    val confirmNewPassword: String = "",
    val confirmNewPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isPasswordLoading: Boolean = false,
    val success: Boolean = false,
    val profilePicture: String? = null,
    val isUploadingImage: Boolean = false
)
