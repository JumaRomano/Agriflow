package com.agriflow.app.features.auth

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)
