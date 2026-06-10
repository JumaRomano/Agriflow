package com.agriflow.app.features.profile

import com.agriflow.app.features.auth.UserRole

data class ProfileState(
    val name: String = "John Doe",
    val role: UserRole = UserRole.FARMER,
    val email: String = "john.doe@agriflow.com",
    val isLoading: Boolean = false
)
