/**
 * User actions triggered from the Edit Profile UI, processed by [EditProfileViewModel].
 */
package com.agriflow.app.features.profile

sealed interface EditProfileAction {
    data class OnUsernameChanged(val username: String) : EditProfileAction
    data class OnFirstNameChanged(val firstName: String) : EditProfileAction
    data class OnMiddleNameChanged(val middleName: String) : EditProfileAction
    data class OnSurNameChanged(val surName: String) : EditProfileAction
    data class OnEmailChanged(val email: String) : EditProfileAction
    data class OnPhoneNumberChanged(val phoneNumber: String) : EditProfileAction
    data class OnCurrentPasswordChanged(val currentPassword: String) : EditProfileAction
    data class OnNewPasswordChanged(val newPassword: String) : EditProfileAction
    data class OnConfirmNewPasswordChanged(val confirmNewPassword: String) : EditProfileAction
    data object OnSaveClicked : EditProfileAction
    data object OnChangePasswordClicked : EditProfileAction
}
