/**
 * Single-use navigation or display events emitted by the [EditProfileViewModel]
 * to the presentation layer.
 */
package com.agriflow.app.features.profile

sealed interface EditProfileEvent {
    data object SaveSuccess : EditProfileEvent
    data class ShowMessage(val message: String) : EditProfileEvent
}
