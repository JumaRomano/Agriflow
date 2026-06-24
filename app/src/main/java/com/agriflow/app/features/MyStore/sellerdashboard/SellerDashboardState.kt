/**
 * UI State definition representing the screen state for SellerDashboard.
 */
package com.agriflow.app.features.MyStore.sellerdashboard

data class SellerDashboardState(
    val totalRevenue: String = "Ksh 0.00",
    val thisMonthRevenue: String = "Ksh 0.00",
    val totalOrders: Int = 0,
    val thisMonthOrders: Int = 0,
    val pendingOrders: Int = 0,
    val deliveredOrders: Int = 0,
    val activeListings: Int = 0,
    val inventoryAlerts: Int = 0,
    val revenueChangePercent: Double = 0.0,
    val recentOrders: List<RecentOrder> = emptyList(),
    val monthlyBreakdown: List<MonthlyBreakdownDto> = emptyList(),
    val selectedMonthFilter: String = "All Time",
    val displayRevenue: String = "Ksh 0.00",
    val displayOrders: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
