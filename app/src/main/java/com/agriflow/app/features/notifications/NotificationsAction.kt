package com.agriflow.app.features.notifications

sealed interface NotificationsAction {
    data object RefreshNotifications : NotificationsAction
    data object ClearAllNotifications : NotificationsAction
    class MarkAsRead(val notificationId: String) : NotificationsAction
}
