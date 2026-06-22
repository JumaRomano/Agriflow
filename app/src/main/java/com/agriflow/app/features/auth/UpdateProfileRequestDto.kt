/**
 * Data Transfer Object representing the request body payload sent to the backend
 * to update user profile information (e.g., username and phone number).
 */
package com.agriflow.app.features.auth

data class UpdateProfileRequestDto(
    val username: String,
    val firstName: String,
    val middleName: String?,
    val surName: String,
    val phoneNumber: String,
    val email: String
)
