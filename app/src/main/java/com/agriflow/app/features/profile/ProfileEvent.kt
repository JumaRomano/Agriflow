/**
 * Sealed interface representing one-shot UI events emitted by the Profile ViewModel.
 */
package com.agriflow.app.features.profile

import com.agriflow.app.features.auth.UserRole

sealed interface ProfileEvent {
    data object MapsToLogin : ProfileEvent
    data class NavigateToRoleUpgrade(val role: UserRole) : ProfileEvent
}
