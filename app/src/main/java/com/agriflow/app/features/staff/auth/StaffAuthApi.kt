package com.agriflow.app.features.staff.auth

import com.agriflow.app.features.auth.AuthResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface StaffAuthApi {
    @POST("staff/auth/login")
    suspend fun staffLogin(
        @Body request: StaffLoginRequestDto
    ): Response<AuthResponseDto>
}
