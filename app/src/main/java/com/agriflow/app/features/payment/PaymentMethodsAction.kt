/**
 * Sealed interface representing user actions and UI events for the PaymentMethods flow.
 */
package com.agriflow.app.features.payment

sealed interface PaymentMethodsAction {
    data object OnNavigateBack : PaymentMethodsAction
    data class OnAddPaymentMethod(val detail: String) : PaymentMethodsAction
    data class OnDeletePaymentMethod(val methodId: String) : PaymentMethodsAction
    data class OnSetDefaultPaymentMethod(val methodId: String) : PaymentMethodsAction
    data class OnShowAddDialog(val show: Boolean) : PaymentMethodsAction
    data object OnClearMessages : PaymentMethodsAction
}
