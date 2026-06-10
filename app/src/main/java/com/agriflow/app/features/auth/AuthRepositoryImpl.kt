package com.agriflow.app.features.auth

import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.core.network.safeApiCall
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.EmptyResult
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.toAuthSession
import com.agriflow.app.features.auth.AuthApi
import com.agriflow.app.features.auth.LoginRequestDto
import com.agriflow.app.features.auth.RegisterRequestDto
import com.agriflow.app.features.auth.AuthSession
import com.agriflow.app.features.auth.UserRole
import com.agriflow.app.features.auth.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenRepository: TokenRepository
) : AuthRepository {

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthSession, DataError.Network> {
        // Repository implementation is the bridge between domain and network DTOs.
        // The ViewModel never sees LoginRequestDto or Retrofit Response.
        return safeApiCall {
            authApi.login(
                request = LoginRequestDto(
                    email = email.trim(),
                    password = password
                )
            )
        }.toAuthSessionResult()
            .also { result -> result.saveTokensIfSuccessful() }
    }

    override suspend fun register(
        username: String,
        email: String,
        phoneNumber: String?,
        password: String,
        firstName: String,
        surName: String
    ): Result<AuthSession, DataError.Network> {
        return safeApiCall {
            authApi.register(
                request = RegisterRequestDto(
                    username = username.trim(),
                    email = email.trim(),
                    phoneNumber = phoneNumber?.trim()?.takeIf(String::isNotBlank),
                    password = password,
                    firstName = firstName.trim(),
                    surName = surName.trim(),
                    role = "BUYER"
                )
            )
        }.toAuthSessionResult()
            .also { result -> result.saveTokensIfSuccessful() }
    }

    override suspend fun upgradeRole(
        role: UserRole,
        businessName: String,
        businessEmail: String,
        phoneNumber: String
    ): Result<AuthSession, DataError.Network> {
        return safeApiCall {
            authApi.upgradeRole(
                request = UpgradeRoleRequestDto(
                    role = role.name,
                    businessName = businessName.trim(),
                    businessEmail = businessEmail.trim(),
                    phoneNumber = phoneNumber.trim()
                )
            )
        }.toAuthSessionResult()
            .also { result -> result.saveTokensIfSuccessful() }
    }

    override suspend fun logout(): EmptyResult<DataError.Local> {
        // Local logout is currently just token cleanup. Later this can also call a revoke endpoint.
        tokenRepository.clearTokens()
        return Result.Success(Unit)
    }

    override suspend fun getBusinessDetails(): Result<BusinessDetailsResponseDto, DataError.Network> {
        return safeApiCall {
            authApi.getBusinessDetails()
        }
    }

    private fun Result<com.agriflow.app.features.auth.AuthResponseDto, DataError.Network>.toAuthSessionResult():
        Result<AuthSession, DataError.Network> {
        // DTO -> domain mapping happens here. If the backend response shape changes, update the mapper.
        return when (this) {
            is Result.Error -> Result.Error(error)
            is Result.Success -> {
                val session = data.toAuthSession()
                if (session != null) {
                    Result.Success(session)
                } else {
                    Result.Error(DataError.Network.SERIALIZATION)
                }
            }
        }
    }

    private fun Result<AuthSession, DataError.Network>.saveTokensIfSuccessful() {
        if (this is Result.Success) {
            // Persist tokens only after the response has been validated and mapped into AuthSession.
            tokenRepository.saveTokens(
                accessToken = data.tokens.accessToken,
                refreshToken = data.tokens.refreshToken
            )
        }
    }
}
