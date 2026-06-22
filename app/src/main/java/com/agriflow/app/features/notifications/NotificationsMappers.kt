package com.agriflow.app.features.notifications

import com.agriflow.app.core.util.TimeParser

fun NotificationDto.toEntity(fallbackTimestamp: Long): NotificationEntity {
    return NotificationEntity(
        notificationId = id ?: java.util.UUID.randomUUID().toString(),
        title = title ?: "Notification",
        body = body ?: "",
        timestamp = createdAt?.let { TimeParser.parseIsoStringToMillis(it) } ?: fallbackTimestamp,
        isRead = read ?: false
    )
}
