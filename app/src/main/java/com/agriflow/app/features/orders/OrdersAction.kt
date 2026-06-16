/**
 * Sealed interface representing user actions and UI events for the Orders flow.
 */
package com.agriflow.app.features.orders

sealed interface OrdersAction {
    data object RefreshOrders : OrdersAction
    data class ToggleOrderDetails(val orderId: String) : OrdersAction
    data class UpdateShipmentStatus(
        val orderId: String,
        val status: String,
        val trackingNumber: String? = null,
        val carrier: String? = null
    ) : OrdersAction
}
