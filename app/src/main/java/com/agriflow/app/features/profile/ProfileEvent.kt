package com.agriflow.app.features.profile

import com.agriflow.app.features.auth.UserRole

sealed interface ProfileEvent {
    data object MapsToLogin : ProfileEvent
    data class NavigateToRoleUpgrade(val role: UserRole) : ProfileEvent
}
