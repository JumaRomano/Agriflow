/**
 * ViewModel managing the business logic and UI state for the Profile feature.
 */
package com.agriflow.app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.features.auth.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.agriflow.app.features.notifications.NotificationsRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.agriflow.app.features.auth.AuthRepository
import com.agriflow.app.features.auth.UserDao
import com.agriflow.app.core.util.Result

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val notificationsRepository: NotificationsRepository,
    private val authRepository: AuthRepository,
    private val userDao: UserDao
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _events = Channel<ProfileEvent>()
    val events = _events.receiveAsFlow()


    init {
        // Observe token repository for active role updates
        viewModelScope.launch {
            tokenRepository.getUserFlow().collect { user ->
                if (user != null) {
                    _state.update {
                        it.copy(
                            role = user.role
                        )
                    }
                    // Fetch business profile when in a business role
                    if (user.role == UserRole.FARMER || user.role == UserRole.SUPPLIER) {
                        loadBusinessDetails()
                    } else {
                        // Clear business info when in buyer mode
                        _state.update {
                            it.copy(
                                businessName = null,
                                businessEmail = null,
                                businessPhone = null,
                                businessLogoUrl = null,
                                businessCounty = null,
                                businessJoinDate = null,
                                businessApprovalStatus = null
                            )
                        }
                    }
                }
            }
        }

        // Observe database cache for reactive updates (name, email, profile picture)
        viewModelScope.launch {
            userDao.observeCurrentUser().collect { cachedUser ->
                if (cachedUser != null) {
                    _state.update {
                        it.copy(
                            name = cachedUser.username,
                            email = cachedUser.email,
                            profilePicture = cachedUser.profilePicture

                        )
                    }
                }
            }
        }

        // Fetch fresh user profile details from backend to update database cache
        viewModelScope.launch {
            authRepository.getCurrentUser()
        }
    }

    private fun loadBusinessDetails() {
        viewModelScope.launch {
            when (val result = authRepository.getBusinessDetails()) {
                is Result.Success -> {
                    val biz = result.data
                    _state.update {
                        it.copy(
                            businessName = biz.businessName,
                            businessEmail = biz.businessEmail,
                            businessPhone = biz.businessPhone,
                            businessLogoUrl = biz.businessProfile,
                            businessCounty = biz.county,
                            businessJoinDate = biz.joinDate,
                            businessApprovalStatus = biz.approvalStatus
                        )
                    }
                }
                is Result.Error -> {
                    // Non-critical: profile still shows user info on failure
                }
            }
        }
    }


    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnLogoutClicked -> logout()
            ProfileAction.OnRefreshProfile -> {
                viewModelScope.launch {
                    authRepository.getCurrentUser()
                    val activeRole = tokenRepository.getActiveRole()
                    if (activeRole == UserRole.FARMER || activeRole == UserRole.SUPPLIER) {
                        loadBusinessDetails()
                    }
                }
            }
            ProfileAction.OnSwitchAccountClicked -> {
                viewModelScope.launch {
                    val activeRole = tokenRepository.getActiveRole()
                    if (activeRole == UserRole.FARMER || activeRole == UserRole.SUPPLIER) {
                        tokenRepository.setActiveRole(UserRole.BUYER)
                    } else {
                        val actualRole = tokenRepository.getActualRole()
                        if (actualRole == UserRole.FARMER || actualRole == UserRole.SUPPLIER) {
                            tokenRepository.setActiveRole(actualRole)
                        } else {
                            _state.update { it.copy(isLoading = true) }
                            when (val result = authRepository.getBusinessDetails()) {
                                is Result.Success -> {
                                    _state.update { it.copy(isLoading = false) }
                                    val businessDetails = result.data
                                    val approvalStatus = businessDetails.approvalStatus
                                    if (approvalStatus != null) {
                                        var registeredRole = tokenRepository.getRegisteredBusinessRole()
                                        if (registeredRole == UserRole.UNKNOWN) {
                                            registeredRole = UserRole.FARMER
                                            tokenRepository.saveRegisteredBusinessRole(registeredRole)
                                        }
                                        if (approvalStatus.equals("APPROVED", ignoreCase = true)) {
                                            _state.update { it.copy(isLoading = true) }
                                            when (val refreshResult = authRepository.refreshToken()) {
                                                is Result.Success -> {
                                                    _state.update { it.copy(isLoading = false) }
                                                    tokenRepository.setActiveRole(registeredRole)
                                                }
                                                is Result.Error -> {
                                                    _state.update { it.copy(isLoading = false) }
                                                    _events.send(ProfileEvent.ShowToast("Failed to switch: Token refresh failed"))
                                                }
                                            }
                                        } else {
                                            _events.send(ProfileEvent.NavigateToRoleUpgrade(registeredRole))
                                        }
                                    } else {
                                        _events.send(ProfileEvent.NavigateToRoleUpgrade(UserRole.BUYER))
                                    }
                                }
                                is Result.Error -> {
                                    _state.update { it.copy(isLoading = false) }
                                    _events.send(ProfileEvent.NavigateToRoleUpgrade(UserRole.BUYER))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // In ProfileState.kt


    private fun logout() {
        _state.update { it.copy(isLoading = true) }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val fcmToken = if (task.isSuccessful) task.result else null
            viewModelScope.launch {
                if (!fcmToken.isNullOrBlank()) {
                    notificationsRepository.unregisterDeviceToken(fcmToken)
                }
                authRepository.logout()
                _state.update { it.copy(isLoading = false) }
                _events.send(ProfileEvent.MapsToLogin)
            }
        }
    }
}
