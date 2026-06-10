package com.agriflow.app.features.profile

sealed interface ProfileAction {
    data object OnLogoutClicked : ProfileAction
    data object OnSwitchAccountClicked : ProfileAction
}
