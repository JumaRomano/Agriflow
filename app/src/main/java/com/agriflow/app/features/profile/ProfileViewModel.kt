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
import com.agriflow.app.core.util.Result

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val notificationsRepository: NotificationsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _events = Channel<ProfileEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            tokenRepository.getUserFlow().collect { user ->
                if (user != null) {
                    _state.update {
                        it.copy(
                            name = user.username,
                            email = user.email,
                            role = user.role
                        )
                    }
                }
            }
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.OnLogoutClicked -> logout()
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
                                            tokenRepository.setActiveRole(registeredRole)
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

    private fun logout() {
        _state.update { it.copy(isLoading = true) }
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val fcmToken = if (task.isSuccessful) task.result else null
            viewModelScope.launch {
                if (!fcmToken.isNullOrBlank()) {
                    notificationsRepository.unregisterDeviceToken(fcmToken)
                }
                tokenRepository.clearTokens()
                _state.update { it.copy(isLoading = false) }
                _events.send(ProfileEvent.MapsToLogin)
            }
        }
    }
}
