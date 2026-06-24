package com.agriflow.app.features.MyStore.sellerdashboard

import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result

interface SellerDashboardRepository {
    suspend fun getDashboardAnalytics(): Result<SellerDashboardAnalyticsDto, DataError.Network>
}
