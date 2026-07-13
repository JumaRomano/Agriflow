package com.agriflow.app.features.staff.auth

import com.agriflow.app.features.auth.AuthResponseDto
import com.agriflow.app.features.auth.ChangePasswordRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StaffAuthApi {
    @POST("staff/auth/login")
    suspend fun staffLogin(
        @Body request: StaffLoginRequestDto
    ): Response<AuthResponseDto>

    @POST("staff/auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequestDto
    ): Response<Unit>

    @GET("staff/me")
    suspend fun staffprofile(): Response<StaffProfileDto>

    @GET("staff/assignments/pending")
    suspend fun pendingAssignments(): Response<List<AssignmentsDto>>

    @GET("staff/assignments/completed")
    suspend fun CompletedAssignments(): Response<List<AssignmentsDto>>

    @GET("staff/assignments/all")
    suspend fun AllAssignments(): Response<List<AssignmentsDto>>

    @POST("staff/assignments/{assignmentId}/verify")
    suspend fun verifyAssignment(
        @Path("assignmentId") assignmentId: String,
        @Body request: VerifyAssignmentRequest
    ): Response<Unit>
}

