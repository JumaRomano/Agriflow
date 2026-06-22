/**
 * UI State definition representing the screen state for SellerDashboard.
 */
package com.agriflow.app.features.MyStore.sellerdashboard

data class SellerDashboardState(
    val totalRevenue: String = "ksh0.00",
    val pendingOrders: Int = 0,
    val activeListings: Int = 0,
    val inventoryAlerts: Int = 0,
    val recentOrders: List<RecentOrder> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
