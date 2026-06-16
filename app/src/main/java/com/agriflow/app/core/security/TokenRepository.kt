/**
 * Repository interface for managing data transactions related to Token.
 */
package com.agriflow.app.core.security

import com.agriflow.app.features.auth.User
import com.agriflow.app.features.auth.UserRole
import kotlinx.coroutines.flow.Flow

interface TokenRepository {
    fun saveTokens(accessToken: String, refreshToken: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clearTokens()
    
    // Reactive flows exposing clean models to the presentation layer
    fun getUserRoleFlow(): Flow<UserRole>
    fun getUserFlow(): Flow<User?>

    fun getActualRole(): UserRole
    fun getActiveRole(): UserRole
    fun setActiveRole(role: UserRole)

    fun getRegisteredBusinessRole(): UserRole
    fun saveRegisteredBusinessRole(role: UserRole)
}
