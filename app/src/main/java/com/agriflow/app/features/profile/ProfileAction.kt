/**
 * Sealed interface representing user actions and UI events for the Profile flow.
 */
package com.agriflow.app.features.profile

sealed interface ProfileAction {
    data object OnLogoutClicked : ProfileAction
    data object OnSwitchAccountClicked : ProfileAction
    data object OnRefreshProfile : ProfileAction
}
