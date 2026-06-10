package com.agriflow.app.features.marketplace.MyStore.sellerdashboard

sealed interface SellerDashboardEvent {
    data object NavigateToExportReport : SellerDashboardEvent
    data class NavigateToOrderDetail(val orderId: String) : SellerDashboardEvent
    data class ShowSnackbarMessage(val message: String) : SellerDashboardEvent
    data object NavigateToAddProduct : SellerDashboardEvent
}
