package com.agriflow.app.features.auth

data class RegisterRequestDto(
    val username: String,
    val email: String,
    val phoneNumber: String?,
    val password: String,
    val firstName: String,
    val surName: String,
    val role: String = "BUYER"
)
