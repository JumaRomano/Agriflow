/**
 * Core helper component: AuthMappers.
 */
package com.agriflow.app.features.auth

import com.agriflow.app.features.auth.AuthResponseDto
import com.agriflow.app.features.auth.AuthSession
import com.agriflow.app.features.auth.AuthTokens
import com.agriflow.app.features.auth.User
import com.agriflow.app.features.auth.UserRole
fun AuthResponseDto.toAuthSession(): AuthSession? {
    val accessToken = accessToken?.takeIf(String::isNotBlank) ?: return null
    val refreshToken = refreshToken?.takeIf(String::isNotBlank) ?: return null
    val name = username?.takeIf(String::isNotBlank) ?: "User"

    val user = User(
        id = "", // Can be extracted from JWT 'sub' if needed in the future
        username = name,
        email = "", 
        phoneNumber = null,
        role = role.toUserRole()
    )

    return AuthSession(
        user = user,
        tokens = AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    )
}

private fun String?.toUserRole(): UserRole {
    return when (this?.trim()?.removePrefix("ROLE_")?.uppercase()) {
        UserRole.FARMER.name -> UserRole.FARMER
        UserRole.SUPPLIER.name -> UserRole.SUPPLIER
        "SELLER" -> UserRole.SUPPLIER
        UserRole.BUYER.name -> UserRole.BUYER
        "BUYER" -> UserRole.BUYER
        UserRole.ADMIN.name -> UserRole.ADMIN
        else -> UserRole.UNKNOWN
    }
}
