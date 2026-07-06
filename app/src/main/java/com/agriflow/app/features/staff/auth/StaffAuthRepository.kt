package com.agriflow.app.features.staff.auth

import com.agriflow.app.core.network.safeApiCall
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.AuthSession
import com.agriflow.app.features.auth.UserDao
import com.agriflow.app.features.auth.ChangePasswordRequestDto
import com.agriflow.app.features.auth.toAuthSession
import com.agriflow.app.features.auth.toEntity
import javax.inject.Inject

interface StaffAuthRepository {
    suspend fun login(
        username: String,
        password: String
    ): Result<AuthSession, DataError.Network>

    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Result<Unit, DataError.Network>
}

class StaffAuthRepositoryImpl @Inject constructor(
    private val staffAuthApi: StaffAuthApi,
    private val tokenRepository: TokenRepository,
    private val userDao: UserDao
) : StaffAuthRepository {

    override suspend fun login(
        username: String,
        password: String
    ): Result<AuthSession, DataError.Network> {
        val result = safeApiCall {
            staffAuthApi.staffLogin(
                request = StaffLoginRequestDto(
                    username = username.trim(),
                    password = password
                )
            )
        }

        return when (result) {
            is Result.Error -> Result.Error(result.error)
            is Result.Success -> {
                val session = result.data.toAuthSession()
                if (session != null) {
                    // Save tokens exactly like normal login
                    tokenRepository.saveTokens(
                        accessToken = session.tokens.accessToken,
                        refreshToken = session.tokens.refreshToken,
                        email = session.user.email ?: username.trim(), // fallback if no email
                        role = session.user.role
                    )
                    userDao.insertUser(session.user.toEntity())
                    Result.Success(session)
                } else {
                    Result.Error(DataError.Network.SERIALIZATION)
                }
            }
        }
    }

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Result<Unit, DataError.Network> {
        return safeApiCall {
            staffAuthApi.changePassword(
                request = ChangePasswordRequestDto(
                    oldPassword = oldPassword,
                    newPassword = newPassword,
                    confirmNewPassword = confirmNewPassword
                )
            )
        }
    }
}
