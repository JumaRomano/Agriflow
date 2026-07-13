/**
 * UI State definition representing the screen state for Profile.
 */
package com.agriflow.app.features.profile

import com.agriflow.app.features.auth.UserRole

// In ProfileState.kt
data class ProfileState(
    val name: String = "John Doe",
    val role: UserRole = UserRole.FARMER,
    val email: String = "john.doe@agriflow.com",
    val profilePicture: String? = null,
    val isLoading: Boolean = false,
    // Business info – populated when role is FARMER or SUPPLIER
    val businessName: String? = null,
    val businessEmail: String? = null,
    val businessPhone: String? = null,
    val businessLogoUrl: String? = null,
    val businessCounty: String? = null,
    val businessJoinDate: String? = null,
    val businessApprovalStatus: String? = null
)
