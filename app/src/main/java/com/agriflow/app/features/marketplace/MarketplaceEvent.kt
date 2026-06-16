/**
 * Sealed interface representing one-shot UI events emitted by the Marketplace ViewModel.
 */
package com.agriflow.app.features.marketplace

sealed interface MarketplaceEvent {
    data class ShowMessage(val message: String) : MarketplaceEvent
    data class NavigateToProductDetails(val productId: String) : MarketplaceEvent
    data object NavigateToCart : MarketplaceEvent
}
