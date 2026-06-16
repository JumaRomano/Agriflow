/**
 * Sealed interface representing one-shot UI events emitted by the ProductDetails ViewModel.
 */
package com.agriflow.app.features.marketplace.productdetails

sealed interface ProductDetailsEvent {
    data class ShowSnackbar(val message: String) : ProductDetailsEvent
    data object MapsBack : ProductDetailsEvent
}
