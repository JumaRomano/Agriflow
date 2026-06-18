package com.agriflow.app.features.notifications

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.Query

interface NotificationsApi {
    @POST("notifications/device-token")
    suspend fun registerDeviceToken(
        @Body request: DeviceTokenRequestDto
    ): Response<Unit>

    @DELETE("notifications/device-token")
    suspend fun unregisterDeviceToken(
        @Query("fcmToken") fcmToken: String
    ): Response<Unit>

    @GET("notifications/my-notifications")
    suspend fun getMyNotifications(): Response<List<NotificationDto>>
}
