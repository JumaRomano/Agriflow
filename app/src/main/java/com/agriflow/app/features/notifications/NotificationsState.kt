package com.agriflow.app.features.notifications

data class NotificationsState(
    val isLoading: Boolean = false,
    val notifications: List<NotificationItem> = emptyList(),
    val errorMessage: String? = null
)
