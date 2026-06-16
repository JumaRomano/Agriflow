/**
 * Sealed interface representing one-shot UI events emitted by the Orders ViewModel.
 */
package com.agriflow.app.features.orders

sealed interface OrdersEvent {
    data class ShowSnackbar(val message: String) : OrdersEvent
}
