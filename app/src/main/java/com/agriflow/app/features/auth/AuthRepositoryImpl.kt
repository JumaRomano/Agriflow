/**
 * Repository implementation of [AuthRepository] managing remote and local data operations.
 */
package com.agriflow.app.features.auth

import com.agriflow.app.core.security.TokenRepository
import kotlinx.coroutines.flow.firstOrNull
import com.agriflow.app.core.network.safeApiCall
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.EmptyResult
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.login.LoginRequestDto
import com.agriflow.app.features.auth.otp.OtpType
import com.agriflow.app.features.auth.otp.PasswordResetRequestDto
import com.agriflow.app.features.auth.otp.SendOtpRequestDto
import com.agriflow.app.features.auth.otp.VerifyOtpRequestDto
import com.agriflow.app.features.auth.otp.VerifyOtpResponseDto
import com.agriflow.app.features.auth.register.RegisterRequestDto
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenRepository: TokenRepository,
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthSession, DataError.Network> {
        val result = safeApiCall {
            authApi.login(
                request = LoginRequestDto(
                    email = email.trim(),
                    password = password
                )
            )
        }.toAuthSessionResult()
        
        if (result is Result.Success) {
            val session = result.data
            tokenRepository.saveTokens(
                accessToken = session.tokens.accessToken,
                refreshToken = session.tokens.refreshToken,
                email = email.trim()
            )
            userDao.insertUser(session.user.toEntity())
        }
        return result
    }

    override suspend fun register(
        username: String,
        email: String,
        phoneNumber: String?,
        password: String,
        firstName: String,
        surName: String
    ): Result<AuthSession, DataError.Network> {
        val result = safeApiCall {
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
        
        if (result is Result.Success) {
            val session = result.data
            tokenRepository.saveTokens(
                accessToken = session.tokens.accessToken,
                refreshToken = session.tokens.refreshToken,
                email = email.trim()
            )
            userDao.insertUser(session.user.toEntity())
        }
        return result
    }

    override suspend fun upgradeRole(
        role: UserRole,
        businessName: String,
        businessEmail: String,
        businessPhone: String
    ): Result<BusinessDetailsResponseDto, DataError.Network> {
        return safeApiCall {
            authApi.upgradeRole(
                request = UpgradeRoleRequestDto(
                    role = role.name,
                    businessName = businessName.trim(),
                    businessEmail = businessEmail.trim(),
                    phoneNumber = businessPhone.trim()
                )
            )
        }
    }

    override suspend fun logout(): EmptyResult<DataError.Local> {
        tokenRepository.clearTokens()
        userDao.clearUser()
        return Result.Success(Unit)
    }

    override suspend fun getBusinessDetails(): Result<BusinessDetailsResponseDto, DataError.Network> {
        return safeApiCall {
            authApi.getBusinessDetails()
        }
    }

    override suspend fun sendOtp(
        email: String,
        type: OtpType
    ): EmptyResult<DataError.Network> {
        return safeApiCall {
            authApi.sendOtp(
                SendOtpRequestDto(
                    email = email.trim(),
                    type = type
                )
            )
        }
    }

    override suspend fun verifyOtp(
        email: String,
        otpCode: String,
        type: OtpType
    ): Result<VerifyOtpResponseDto, DataError.Network> {
        return safeApiCall {
            authApi.verifyOtp(
                VerifyOtpRequestDto(
                    email = email.trim(),
                    otpCode = otpCode.trim(),
                    type = type
                )
            )
        }
    }

    override suspend fun passwordReset(
        email: String,
        newPassword: String,
        confirmPassword: String,
        resetToken: String
    ): EmptyResult<DataError.Network> {
        return safeApiCall {
            authApi.passwordReset(
                PasswordResetRequestDto(
                    email = email.trim(),
                    newPassword = newPassword,
                    confirmPassword = confirmPassword,
                    resetToken = resetToken
                )
            )
        }
    }

    override suspend fun updateProfile(
        userId: String,
        username: String,
        firstName: String,
        middleName: String?,
        surName: String,
        phoneNumber: String,
        email: String
    ): EmptyResult<DataError.Network> {
        val result = safeApiCall {
            authApi.updateProfile(
                userId = userId,
                request = UpdateProfileRequestDto(
                    username = username.trim(),
                    firstName = firstName.trim(),
                    middleName = middleName?.trim()?.takeIf(String::isNotBlank),
                    surName = surName.trim(),
                    phoneNumber = phoneNumber.trim(),
                    email = email.trim()
                )
            )
        }

        if (result is Result.Success) {
            val currentRole = tokenRepository.getUserFlow().firstOrNull()?.role ?: UserRole.UNKNOWN
            val updatedUser = UserEntity(
                id = userId,
                username = username.trim(),
                email = email.trim(),
                phoneNumber = phoneNumber.trim(),
                role = currentRole,
                firstName = firstName.trim(),
                middleName = middleName?.trim()?.takeIf(String::isNotBlank),
                surName = surName.trim()
            )
            userDao.insertUser(updatedUser)
        }
        return result
    }

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): EmptyResult<DataError.Network> {
        return safeApiCall {
            authApi.changePassword(
                ChangePasswordRequestDto(
                    oldPassword = oldPassword,
                    newPassword = newPassword,
                    confirmNewPassword = confirmNewPassword
                )
            )
        }
    }

    override suspend fun getCurrentUser(): Result<User, DataError.Network> {
        val result = safeApiCall {
            authApi.getCurrentUser()
        }
        return when (result) {
            is Result.Success -> Result.Success(result.data.toUser())
            is Result.Error -> Result.Error(result.error)
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
}
