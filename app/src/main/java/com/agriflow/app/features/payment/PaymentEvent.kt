/**
 * Sealed interface representing one-shot UI events emitted by the Payment ViewModel.
 */
package com.agriflow.app.features.payment

sealed interface PaymentEvent {
    data object NavigateToOrders : PaymentEvent
    data object NavigateBack : PaymentEvent
    data class ShowSnackbar(val message: String) : PaymentEvent
}
