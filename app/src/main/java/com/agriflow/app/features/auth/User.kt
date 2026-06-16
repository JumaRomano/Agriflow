/**
 * Represents the class [User] providing core functionality within the application.
 */
package com.agriflow.app.features.auth

data class User(
    val id: String,
    val username: String,
    val email: String,
    val phoneNumber: String?,
    val role: UserRole
)
