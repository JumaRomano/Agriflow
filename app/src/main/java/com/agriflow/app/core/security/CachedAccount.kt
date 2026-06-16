/**
 * Represents the class [CachedAccount] providing core functionality within the application.
 */
package com.agriflow.app.core.security

import com.agriflow.app.features.auth.UserRole

data class CachedAccount(
    val userId: String,
    val username: String,
    val email: String,
    val role: UserRole,
    val accessToken: String,
    val refreshToken: String
)
