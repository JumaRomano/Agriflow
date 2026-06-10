package com.agriflow.app.features.auth

data class AuthSession(
    val user: User,
    val tokens: AuthTokens
)
