/**
 * Sealed interface representing user actions and UI events for the SellerDashboard flow.
 */
package com.agriflow.app.features.MyStore.sellerdashboard

sealed interface SellerDashboardAction {
    data object OnExportReportClicked : SellerDashboardAction
    data class OnOrderClicked(val orderId: String) : SellerDashboardAction
    data object OnAddProductClicked : SellerDashboardAction
}
