/**
 * Repository interface for managing data transactions related to Auth.
 */
package com.agriflow.app.features.auth

import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.EmptyResult
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.otp.OtpType
import com.agriflow.app.features.auth.otp.VerifyOtpResponseDto

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
    ): Result<BusinessDetailsResponseDto, DataError.Network>

    suspend fun getBusinessDetails(): Result<BusinessDetailsResponseDto, DataError.Network>

    suspend fun logout(): EmptyResult<DataError.Local>

    suspend fun sendOtp(
        email: String,
        type: OtpType
    ): EmptyResult<DataError.Network>

    suspend fun verifyOtp(
        email: String,
        otpCode: String,
        type: OtpType
    ): Result<VerifyOtpResponseDto, DataError.Network>

    suspend fun passwordReset(
        email: String,
        newPassword: String,
        confirmPassword: String,
        resetToken: String
    ): EmptyResult<DataError.Network>

    suspend fun updateProfile(
        userId: String,
        username: String,
        firstName: String,
        middleName: String?,
        surName: String,
        phoneNumber: String,
        email: String,
        profilePicture: String?
    ): EmptyResult<DataError.Network>

    suspend fun uploadProfileImage(
        file: okhttp3.MultipartBody.Part
    ): Result<ProfileImageResponseDto, DataError.Network>

    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): EmptyResult<DataError.Network>

    suspend fun getCurrentUser(): Result<User, DataError.Network>
}
