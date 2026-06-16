/**
 * Sealed interface representing user actions and UI events for the Cart flow.
 */
package com.agriflow.app.features.cart.presentation

sealed interface CartAction {
    data object FetchCart : CartAction
    data class UpdateQuantity(val productId: String, val itemId: String, val newQuantity: Double) : CartAction
    data class RemoveItem(val itemId: String) : CartAction
    data object ClearCart : CartAction
    data object Checkout : CartAction
}
