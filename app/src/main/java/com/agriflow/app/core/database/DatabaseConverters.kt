package com.agriflow.app.core.database

import androidx.room.TypeConverter
import com.agriflow.app.features.auth.UserRole
import com.agriflow.app.features.marketplace.MyStore.sellerdashboard.OrderStatus
import java.util.Date

class DatabaseConverters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String {
        return role.name
    }

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return try {
            UserRole.valueOf(value)
        } catch (e: IllegalArgumentException) {
            UserRole.UNKNOWN
        }
    }

    @TypeConverter
    fun fromOrderStatus(status: OrderStatus): String {
        return status.name
    }

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus {
        return try {
            OrderStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            OrderStatus.PENDING
        }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}
