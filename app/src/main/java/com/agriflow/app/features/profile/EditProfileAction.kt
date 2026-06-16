/**
 * User actions triggered from the Edit Profile UI, processed by [EditProfileViewModel].
 */
package com.agriflow.app.features.profile

sealed interface EditProfileAction {
    data class OnUsernameChanged(val username: String) : EditProfileAction
    data class OnPhoneNumberChanged(val phoneNumber: String) : EditProfileAction
    data object OnSaveClicked : EditProfileAction
}
