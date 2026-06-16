/**
 * Sealed interface representing one-shot UI events emitted by the Home ViewModel.
 */
package com.agriflow.app.features.homescreen

/**
 * One-time side-effects (Events) sent from the ViewModel to the UI.
 */
sealed interface HomeEvent {
    data object NavigateToMarketplace : HomeEvent
    data object NavigateToCart : HomeEvent
    data class ShowMessage(val message: String) : HomeEvent
    data object NavigateToNotification : HomeEvent
    data object NavigateToWallet : HomeEvent

    data class NavigateToProductDetails(val productId: String) : HomeEvent
}
