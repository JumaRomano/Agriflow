/**
 * Data Transfer Object (DTO) used for network request/response serialization.
 */
package com.agriflow.app.features.auth

data class LoginRequestDto(
    val email: String,
    val password: String
)
