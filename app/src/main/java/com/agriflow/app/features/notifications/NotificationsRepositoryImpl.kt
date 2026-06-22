package com.agriflow.app.features.notifications

import com.agriflow.app.core.network.safeApiCall
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationsRepositoryImpl @Inject constructor(
    private val notificationsApi: NotificationsApi,
    private val notificationDao: NotificationDao
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
        return when (val result = safeApiCall { notificationsApi.getMyNotifications() }) {
            is Result.Success -> {
                val entities = result.data.map { it.toEntity(System.currentTimeMillis()) }
                notificationDao.insertNotifications(entities)
                Result.Success(result.data)
            }
            is Result.Error -> {
                // Return error but since UI observes DB, existing cached notifications will still display
                result
            }
        }
    }

    override fun observeNotifications(): Flow<List<NotificationEntity>> {
        return notificationDao.observeAllNotifications()
    }

    override suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    override suspend fun clearAllNotifications() {
        notificationDao.clearNotifications()
    }
}
