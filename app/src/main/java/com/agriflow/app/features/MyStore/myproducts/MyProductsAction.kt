/**
 * Sealed interface representing user actions and UI events for the MyProducts flow.
 */
package com.agriflow.app.features.MyStore.myproducts

sealed interface MyProductsAction {
    data object OnAddProductClicked : MyProductsAction
    data class OnProductClicked(val productId: String) : MyProductsAction
    data class OnEditClicked(val productId: String) : MyProductsAction
    data class OnDeleteClicked(val productId: String) : MyProductsAction
}
