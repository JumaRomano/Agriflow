/**
 * Sealed interface representing user actions and UI events for the Marketplace flow.
 */
package com.agriflow.app.features.marketplace

import com.agriflow.app.features.marketplace.productdetails.Product

sealed interface MarketplaceAction {
    data class SearchQueryChanged(val query: String) : MarketplaceAction
    data class CategorySelected(val category: String) : MarketplaceAction
    data class ProductClicked(val product: Product) : MarketplaceAction
    data object RefreshRequested : MarketplaceAction
    data object CartClicked : MarketplaceAction
}
