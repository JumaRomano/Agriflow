package com.agriflow.app.features.auth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val phoneNumber: String?,
    val role: UserRole,
    val firstName: String? = null,
    val middleName: String? = null,
    val surName: String? = null
)
