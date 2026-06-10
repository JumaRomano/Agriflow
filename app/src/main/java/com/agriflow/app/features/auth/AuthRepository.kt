package com.agriflow.app.features.auth

import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.EmptyResult
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.AuthSession
import com.agriflow.app.features.auth.UserRole
interface AuthRepository {
    suspend fun login(
        email: String,
        password: String
    ): Result<AuthSession, DataError.Network>

    suspend fun register(
        username: String,
        email: String,
        phoneNumber: String?,
        password: String,
        firstName: String,
        surName: String
    ): Result<AuthSession, DataError.Network>

    suspend fun upgradeRole(
        role: UserRole,
        businessName: String,
        businessEmail: String,
        businessPhone: String
    ): Result<AuthSession, DataError.Network>

    suspend fun getBusinessDetails(): Result<BusinessDetailsResponseDto, DataError.Network>

    suspend fun logout(): EmptyResult<DataError.Local>
}
