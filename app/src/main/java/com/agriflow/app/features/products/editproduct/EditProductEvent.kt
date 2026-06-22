/**
 * Sealed interface representing one-shot UI events emitted by the EditProduct ViewModel.
 */
package com.agriflow.app.features.products.editproduct

sealed interface EditProductEvent {
    data object SaveSuccess : EditProductEvent
    data class ShowSnackbar(val message: String) : EditProductEvent
}
