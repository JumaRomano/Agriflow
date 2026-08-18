package com.agriflow.app.features.payment

sealed interface PaymentEvent {
    data object NavigateToOrders : PaymentEvent
    data object NavigateBack : PaymentEvent
    data class ShowSnackbar(val message: String) : PaymentEvent
}
