/**
 * Sealed interface representing one-shot UI events emitted by the SellerDashboard ViewModel.
 */
package com.agriflow.app.features.MyStore.sellerdashboard

sealed interface SellerDashboardEvent {
    data object NavigateToExportReport : SellerDashboardEvent
    data class NavigateToOrderDetail(val orderId: String) : SellerDashboardEvent
    data class ShowSnackbarMessage(val message: String) : SellerDashboardEvent
    data object NavigateToAddProduct : SellerDashboardEvent
}
