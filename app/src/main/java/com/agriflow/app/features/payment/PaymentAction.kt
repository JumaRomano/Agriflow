/**
 * Sealed interface representing user actions and UI events for the Payment flow.
 */
package com.agriflow.app.features.payment

sealed interface PaymentAction {
    data class OnPhoneNumberChanged(val number: String) : PaymentAction
    data class OnDeliveryAddressChanged(val address: String) : PaymentAction
    data class OnDeliveryNotesChanged(val notes: String) : PaymentAction
    data object OnInitiatePayment : PaymentAction
    data object OnDismissSuccessDialog : PaymentAction
    data object OnNavigateBack : PaymentAction
    data object OnRestoreCartAndGoBack : PaymentAction
}

