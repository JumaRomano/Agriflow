/**
 * Sealed interface representing user actions and UI events for the RoleUpgrade flow.
 */
package com.agriflow.app.features.profile.roleUpgrade

import com.agriflow.app.features.auth.UserRole

sealed interface RoleUpgradeAction {
    data class RoleSelected(val role: UserRole) : RoleUpgradeAction
    data class BusinessNameChanged(val name: String) : RoleUpgradeAction
    data class BusinessEmailChanged(val email: String) : RoleUpgradeAction
    data class BusinessPhoneChanged(val phone: String) : RoleUpgradeAction
    data object SubmitClicked : RoleUpgradeAction
    data object SwitchToActiveRole : RoleUpgradeAction
    data class BusinessCountyChanged(val county: String) : RoleUpgradeAction
    data class BusinessProfileSelected(val uri: android.net.Uri) : RoleUpgradeAction
}