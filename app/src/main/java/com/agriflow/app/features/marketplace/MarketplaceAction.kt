package com.agriflow.app.features.marketplace

import com.agriflow.app.features.products.productdetails.Product

sealed interface MarketplaceAction {
    data class SearchQueryChanged(val query: String) : MarketplaceAction
    data class CategorySelected(val category: String) : MarketplaceAction
    data class ProductClicked(val product: Product) : MarketplaceAction
    data object RefreshRequested : MarketplaceAction
    data object CartClicked : MarketplaceAction
}
