/**
 * Sealed interface representing one-shot UI events emitted by the PaymentMethods ViewModel.
 */
package com.agriflow.app.features.payment

sealed interface PaymentMethodsEvent {
    data object NavigateBack : PaymentMethodsEvent
    data class ShowSnackbar(val message: String) : PaymentMethodsEvent
}
