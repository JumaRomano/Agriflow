package com.agriflow.app.features.wallet

sealed interface WalletEvent {
    object NavigateBack : WalletEvent
    data class ShowToast(val message: String) : WalletEvent
}