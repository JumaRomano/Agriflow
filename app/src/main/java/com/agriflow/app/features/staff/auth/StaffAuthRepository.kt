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
import com.agriflow.app.features.auth.UserEntity
import com.agriflow.app.features.auth.UserRole
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

    suspend fun getStaffProfile(): Result<StaffProfileDto, DataError.Network>

    suspend fun getpendingAssignments(): Result<List<AssignmentsDto>, DataError.Network>

    suspend fun getAllAssignments(): Result<List<AssignmentsDto>, DataError.Network>

    suspend fun getCompletedAssignments(): Result<List<AssignmentsDto>, DataError.Network>

    suspend fun verifyAssignment(
        assignmentId: String,
        evidencePhotos: List<String>,
        latitude: Double,
        longitude: Double
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
                        email = session.user.email,
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

    override suspend fun getStaffProfile(): Result<StaffProfileDto, DataError.Network> {
        val result = safeApiCall {
            staffAuthApi.staffprofile()
        }
        if (result is Result.Success) {
            val dto = result.data
            val staffUserEntity = UserEntity(
                id = dto.id,
                username = dto.username,
                email = dto.email,
                phoneNumber = dto.phoneNumber,
                role = UserRole.STAFF,
                firstName = dto.firstName,
                middleName = dto.middleName,
                surName = dto.surName,
                region = dto.county,
                profilePicture = dto.profilePicture,
                status = dto.status
            )
            userDao.clearUser()
            userDao.insertUser(staffUserEntity)
        }
        return result
    }

    override suspend fun getpendingAssignments(): Result<List<AssignmentsDto>, DataError.Network> {
        return safeApiCall {
            staffAuthApi.pendingAssignments()
        }
    }


    override suspend fun getAllAssignments(): Result<List<AssignmentsDto>, DataError.Network>{
        return safeApiCall {
            staffAuthApi.AllAssignments()
        }
    }

    override suspend fun getCompletedAssignments(): Result<List<AssignmentsDto>, DataError.Network>{
        return safeApiCall {
            staffAuthApi.CompletedAssignments()
        }
    }

    override suspend fun verifyAssignment(
        assignmentId: String,
        evidencePhotos: List<String>,
        latitude: Double,
        longitude: Double
    ): Result<Unit, DataError.Network> {
        return safeApiCall {
            staffAuthApi.verifyAssignment(
                assignmentId = assignmentId,
                request = VerifyAssignmentRequest(
                    evidencePhotos = evidencePhotos,
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }
    }
}
