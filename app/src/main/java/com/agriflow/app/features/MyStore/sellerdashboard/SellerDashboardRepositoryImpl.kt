package com.agriflow.app.features.MyStore.sellerdashboard

import com.agriflow.app.core.network.safeApiCall
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import javax.inject.Inject

class SellerDashboardRepositoryImpl @Inject constructor(
    private val api: SellerDashboardApi
) : SellerDashboardRepository {

    override suspend fun getDashboardAnalytics(): Result<SellerDashboardAnalyticsDto, DataError.Network> {
        return safeApiCall {
            api.getDashboardAnalytics()
        }
    }
}
