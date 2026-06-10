package com.agriflow.app.features.payment

sealed interface PaymentMethodsEvent {
    data object NavigateBack : PaymentMethodsEvent
    data class ShowSnackbar(val message: String) : PaymentMethodsEvent
}
