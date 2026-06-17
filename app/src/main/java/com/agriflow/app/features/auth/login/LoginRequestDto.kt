/**
 * Data Transfer Object (DTO) used for network request/response serialization.
 */
package com.agriflow.app.features.auth.login

data class LoginRequestDto(
    val email: String,
    val password: String
)
