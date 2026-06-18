package com.agriflow.app.features.notifications

import com.agriflow.app.core.network.safeApiCall
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import javax.inject.Inject

class NotificationsRepositoryImpl @Inject constructor(
    private val notificationsApi: NotificationsApi
) : NotificationsRepository {
    override suspend fun registerDeviceToken(
        fcmToken: String,
        deviceType: String
    ): Result<Unit, DataError.Network> {
        return safeApiCall {
            notificationsApi.registerDeviceToken(
                DeviceTokenRequestDto(fcmToken = fcmToken, deviceType = deviceType)
            )
        }
    }

    override suspend fun unregisterDeviceToken(fcmToken: String): Result<Unit, DataError.Network> {
        return safeApiCall {
            notificationsApi.unregisterDeviceToken(fcmToken)
        }
    }

    override suspend fun getMyNotifications(): Result<List<NotificationDto>, DataError.Network> {
        return safeApiCall {
            notificationsApi.getMyNotifications()
        }
    }
}
