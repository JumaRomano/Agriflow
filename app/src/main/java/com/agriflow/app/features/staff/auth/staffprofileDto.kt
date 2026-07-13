package com.agriflow.app.features.staff.auth

data class StaffProfileDto(
    val id: String,
    val username: String,
    val firstName: String?,
    val middleName: String?,
    val surName: String?,
    val phoneNumber: String?,
    val role: String?,
    val email: String,
    val county: String? = null,
    val profilePicture: String? = null,
    val status: String? = null
)
