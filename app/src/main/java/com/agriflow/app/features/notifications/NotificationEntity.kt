package com.agriflow.app.features.notifications

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity representing a notification alert.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val notificationId: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean
)
