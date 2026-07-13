/**
 * UI State definition representing the screen state for RoleUpgrade.
 */
package com.agriflow.app.features.profile.roleUpgrade

import com.agriflow.app.features.auth.UserRole

data class RoleUpgradeState(
    val selectedRole: UserRole = UserRole.FARMER, // Must be FARMER or SUPPLIER
    val businessName: String = "",
    val businessEmail: String = "",
    val businessPhone: String = "",
    val approvalStatus: String? = null,
    val walletBalance: Double = 0.0,
    val availableBalance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val isLoading: Boolean = false,
    val isUploadingImage: Boolean = false,
    val errorMessage: String? = null,
    val businessCounty: String = "",
    val businessProfile: String? = null
)