package com.agriflow.app.features.marketplace.MyStore.sellerdashboard

data class SellerDashboardState(
    val totalRevenue: String = "ksh0.00",
    val pendingOrders: Int = 0,
    val activeListings: Int = 0,
    val inventoryAlerts: Int = 0,
    val recentOrders: List<RecentOrder> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
