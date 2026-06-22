/**
 * Sealed interface representing one-shot UI events emitted by the MyProducts ViewModel.
 */
package com.agriflow.app.features.MyStore.myproducts

sealed interface MyProductsEvent {
    data class MapsToAddProduct(val productId: String? = null) : MyProductsEvent
    data class MapsToProductDetails(val productId: String) : MyProductsEvent
    data class ShowSnackbarMessage(val message: String) : MyProductsEvent
}
