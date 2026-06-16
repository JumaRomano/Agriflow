/**
 * Sealed interface representing one-shot UI events emitted by the RoleUpgrade ViewModel.
 */
package com.agriflow.app.features.profile.roleUpgrade

sealed interface RoleUpgradeEvent {
    data object UpgradeSuccess : RoleUpgradeEvent
    data class ShowError(val message: String) : RoleUpgradeEvent
}