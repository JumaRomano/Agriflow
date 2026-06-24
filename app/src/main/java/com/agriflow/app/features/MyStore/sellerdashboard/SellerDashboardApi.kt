package com.agriflow.app.features.MyStore.sellerdashboard

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET

interface SellerDashboardApi {

    @GET("business/analytics/dashboard")
    suspend fun getDashboardAnalytics(): Response<SellerDashboardAnalyticsDto>
}

data class SellerDashboardAnalyticsDto(
    @SerializedName("thisMonthRevenue") val thisMonthRevenue: Double?,
    @SerializedName("thisMonthOrders") val thisMonthOrders: Int?,
    @SerializedName("lastMonthRevenue") val lastMonthRevenue: Double?,
    @SerializedName("lastMonthOrders") val lastMonthOrders: Int?,
    @SerializedName("revenueChange") val revenueChange: Double?,
    @SerializedName("revenueChangePercent") val revenueChangePercent: Double?,
    @SerializedName("totalRevenue") val totalRevenue: Double?,
    @SerializedName("totalOrders") val totalOrders: Int?,
    @SerializedName("pendingOrders") val pendingOrders: Int?,
    @SerializedName("deliveredOrders") val deliveredOrders: Int?,
    @SerializedName("monthlyBreakdown") val monthlyBreakdown: List<MonthlyBreakdownDto>?
)

data class MonthlyBreakdownDto(
    @SerializedName("month") val month: String?,
    @SerializedName("monthLabel") val monthLabel: String?,
    @SerializedName("revenue") val revenue: Double?,
    @SerializedName("orderCount") val orderCount: Int?
)
