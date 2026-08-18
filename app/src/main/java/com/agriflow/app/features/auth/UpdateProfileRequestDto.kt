package com.agriflow.app.features.auth

data class UpdateProfileRequestDto(
    val username: String,
    val firstName: String,
    val middleName: String?,
    val surName: String,
    val phoneNumber: String,
    val email: String,
    val profilePicture: String? = null
)
