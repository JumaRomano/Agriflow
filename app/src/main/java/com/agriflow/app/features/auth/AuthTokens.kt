/**
 * Represents the class [AuthTokens] providing core functionality within the application.
 */
package com.agriflow.app.features.auth

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)
