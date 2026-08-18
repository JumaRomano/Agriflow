package com.agriflow.app.features.auth

data class User(
    val id: String,
    val username: String,
    val email: String,
    val phoneNumber: String?,
    val role: UserRole,
    val firstName: String? = null,
    val middleName: String? = null,
    val surName: String? = null,
    val profilePicture: String? = null
)
