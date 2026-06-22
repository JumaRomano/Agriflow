/**
 * Retrofit API interface defining network endpoints for the Auth service.
 */
package com.agriflow.app.features.auth

import com.agriflow.app.features.auth.login.LoginRequestDto
import com.agriflow.app.features.auth.otp.PasswordResetRequestDto
import com.agriflow.app.features.auth.otp.SendOtpRequestDto
import com.agriflow.app.features.auth.otp.VerifyOtpRequestDto
import com.agriflow.app.features.auth.otp.VerifyOtpResponseDto
import com.agriflow.app.features.auth.register.RegisterRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApi {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<AuthResponseDto>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): Response<AuthResponseDto>

    @POST("businesses/register")
    suspend fun upgradeRole(
        @Body request: UpgradeRoleRequestDto
    ): Response<AuthResponseDto>

    @GET("businesses/me")
    suspend fun getBusinessDetails(): Response<BusinessDetailsResponseDto>

    @POST("notifications/otp/send")
    suspend fun sendOtp(
        @Body request: SendOtpRequestDto
    ): Response<Unit>

    @POST("notifications/otp/verify")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequestDto
    ): Response<VerifyOtpResponseDto>

    @POST("auth/password-reset")
    suspend fun passwordReset(
        @Body request: PasswordResetRequestDto
    ): Response<Unit>

    @PUT("users/{userId}/profile")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Body request: UpdateProfileRequestDto
    ): Response<Unit>

    @POST("users/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequestDto
    ): Response<Unit>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<CurrentUserResponseDto>
}

data class CurrentUserResponseDto(
    val id: String,
    val username: String,
    val firstName: String?,
    val middleName: String?,
    val surName: String?,
    val phoneNumber: String?,
    val role: String?,
    val email: String
)

data class ChangePasswordRequestDto(
    val oldPassword: String,
    val newPassword: String,
    val confirmNewPassword: String
)

data class UpgradeRoleRequestDto(
    val role: String,
    val businessName: String,
    val businessEmail: String,
    val phoneNumber: String
)
