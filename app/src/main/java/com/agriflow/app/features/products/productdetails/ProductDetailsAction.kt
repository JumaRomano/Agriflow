/**
 * Sealed interface representing user actions and UI events for the ProductDetails flow.
 */
package com.agriflow.app.features.products.productdetails

sealed interface ProductDetailsAction {
    data object OnIncrementQuantity : ProductDetailsAction
    data object OnDecrementQuantity : ProductDetailsAction
    data object OnAddToCart : ProductDetailsAction
    data object OnNavigateBack : ProductDetailsAction
    data class OnSupplierClick(val businessId: String) : ProductDetailsAction
}
