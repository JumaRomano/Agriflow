package com.agriflow.app.features.orders

sealed interface OrdersEvent {
    data class ShowSnackbar(val message: String) : OrdersEvent
}
