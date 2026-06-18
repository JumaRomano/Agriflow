package com.agriflow.app.features.notifications

import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result

interface NotificationsRepository {
    suspend fun registerDeviceToken(fcmToken: String, deviceType: String): Result<Unit, DataError.Network>
    suspend fun unregisterDeviceToken(fcmToken: String): Result<Unit, DataError.Network>
    suspend fun getMyNotifications(): Result<List<NotificationDto>, DataError.Network>
}
