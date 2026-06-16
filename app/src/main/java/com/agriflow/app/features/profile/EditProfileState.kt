/**
 * State representation for the Edit Profile UI, holding form values,
 * validation errors, and asynchronous execution states (loading, success).
 */
package com.agriflow.app.features.profile

data class EditProfileState(
    val username: String = "",
    val usernameError: String? = null,
    val email: String = "",
    val phoneNumber: String = "",
    val phoneNumberError: String? = null,
    val isLoading: Boolean = false,
    val success: Boolean = false
)
